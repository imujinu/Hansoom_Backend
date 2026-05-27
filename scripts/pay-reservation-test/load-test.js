import http from 'k6/http';
import { check } from 'k6';
import { Counter, Rate, Trend } from 'k6/metrics';

// 환경변수로 전략과 파라미터 제어
// 전략: pessimistic | optimistic | reentrant | redis (기본값: redis)
const strategy       = __ENV.STRATEGY        || 'redis';
const stockCount     = parseInt(__ENV.STOCK_COUNT      || '100');
const paymentDelayMs = parseInt(__ENV.PAYMENT_DELAY_MS || '500');
const rate           = parseInt(__ENV.RATE             || '50');
const baseUrl        = 'http://localhost:8080/api/test/pay-reservations';

// rate × (paymentDelayMs/1000)만큼 VU가 동시에 블로킹될 수 있으므로 여유 있게 설정
const preAllocatedVUs = rate * 4;
const maxVUs          = rate * 8;

const confirmedCounter = new Counter('confirmed_count');
const failedCounter    = new Counter('failed_count');
const successRate      = new Rate('success_rate');
const confirmLatency   = new Trend('confirm_latency_ms', true); // CONFIRMED 요청의 응답시간

export const options = {
    scenarios: {
        payment_concurrency_test: {
            executor: 'constant-arrival-rate',
            rate: rate,
            timeUnit: '1s',
            duration: '15s',
            preAllocatedVUs: preAllocatedVUs,
            maxVUs: maxVUs,
            gracefulStop: '3s',
        },
    },
    thresholds: {
        http_req_duration: [`p(95)<${paymentDelayMs * 3 + 1000}`],
        http_req_failed: ['rate<0.8'],
    },
};

export function setup() {
    const res = http.post(`${baseUrl}/reset?stockCount=${stockCount}`);
    if (res.status !== 200) {
        throw new Error(`Reset failed: ${res.status} ${res.body}`);
    }
    const body = JSON.parse(res.body);
    const startTime = Date.now();

    console.log(`=== SETUP ===`);
    console.log(`Strategy:       ${strategy}`);
    console.log(`Rate:           ${rate} req/s`);
    console.log(`StockId:        ${body.stockId}`);
    console.log(`InitialStock:   ${body.initialStock}`);
    console.log(`PaymentDelayMs: ${paymentDelayMs}`);

    return { stockId: body.stockId, startTime: startTime };
}

export default function (data) {
    const userId = __VU * 100000 + __ITER;
    const params = { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } };

    const res = http.post(
        `${baseUrl}/${strategy}?stockId=${data.stockId}&userId=${userId}&paymentDelayMs=${paymentDelayMs}`,
        null,
        params
    );

    check(res, {
        'http 200': (r) => r.status === 200,
        'no server error': (r) => r.status !== 500,
    });

    if (res.status === 200) {
        const body = JSON.parse(res.body);
        if (body.result === 'CONFIRMED') {
            confirmedCounter.add(1);
            successRate.add(1);
            confirmLatency.add(res.timings.duration);
        } else {
            failedCounter.add(1);
            successRate.add(0);
        }
    } else {
        failedCounter.add(1);
        successRate.add(0);
    }
}

export function teardown(data) {
    const endTime   = Date.now();
    const elapsedSec = (endTime - data.startTime) / 1000;

    const statusRes = http.get(`${baseUrl}/status/${data.stockId}`);
    if (statusRes.status !== 200) {
        console.error(`Status check failed: ${statusRes.status}`);
        return;
    }
    const db = JSON.parse(statusRes.body);

    // TPS: DB의 확정 주문 수 / 실제 경과 시간
    const confirmedTps   = (db.confirmedCount  / elapsedSec).toFixed(2);
    const totalReqTarget = rate * 15; // 목표 요청 수 (rate × duration)

    console.log(`\n${'='.repeat(55)}`);
    console.log(` RESULT [${strategy.toUpperCase()} / rate=${rate} req/s]`);
    console.log(`${'='.repeat(55)}`);
    console.log(` Elapsed:            ${elapsedSec.toFixed(1)}s`);
    console.log(` Target requests:    ${totalReqTarget}`);
    console.log(` Stock:              ${db.confirmedCount} confirmed / ${db.totalStock} total`);
    console.log(` DB available stock: ${db.availableStock}`);
    console.log(`${'─'.repeat(55)}`);
    console.log(` Orders breakdown:`);
    console.log(`   CONFIRMED : ${db.confirmedOrderCount}`);
    console.log(`   PENDING   : ${db.pendingOrderCount}`);
    console.log(`   FAILED    : ${db.failedOrderCount}`);
    console.log(`   Redis occ.: ${db.redisOccupied}`);
    console.log(`${'─'.repeat(55)}`);
    console.log(` TPS (confirmed/s):  ${confirmedTps}`);
    console.log(`${'='.repeat(55)}`);

    // 정합성 검증
    if (db.confirmedCount > stockCount) {
        console.error(`[FAIL] 초과 확정! ${db.confirmedCount} > ${stockCount}`);
    } else {
        console.log(`[PASS] 정합성 OK: confirmedCount(${db.confirmedCount}) <= stockCount(${stockCount})`);
    }

    const dbBalance = db.availableStock + db.confirmedCount;
    if (dbBalance === db.totalStock) {
        console.log(`[PASS] DB balance OK: ${db.availableStock} + ${db.confirmedCount} = ${db.totalStock}`);
    } else {
        console.log(`[INFO] DB balance: ${db.availableStock} + ${db.confirmedCount} = ${dbBalance} (Redis 전략은 availableStock 미사용)`);
    }
}
