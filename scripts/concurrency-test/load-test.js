import http from 'k6/http';
import { check, sleep } from 'k6';

// 1. 초기화 (재고 설정)
// k6 실행 시 파라미터로 전략을 받음: -e STRATEGY=pessimistic
const strategy = __ENV.STRATEGY || 'nolock';
const stockCount = 100;
const baseUrl = 'http://localhost:8080/api/test/reservations';

export const options = {
    scenarios: {
        concurrency_test: {
            executor: 'constant-arrival-rate',
            rate: 100, // 초당 요청 수
            timeUnit: '1s',
            duration: '10s',
            preAllocatedVUs: 100,
            maxVUs: 200,
        },
    },
};

let stockId;

export function setup() {
    // 테스트 시작 전 재고 리셋
    const res = http.post(`${baseUrl}/reset?stockCount=${stockCount}`);
    const body = JSON.parse(res.body);
    console.log(`Test Setup: Strategy=${strategy}, StockId=${body.stockId}, InitialStock=${stockCount}`);
    return { stockId: body.stockId };
}

export default function (data) {
    const userId = __VU * 1000 + __ITER; // 유니크한 유저 ID 시뮬레이션
    const params = {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
    };
    
    const res = http.post(`${baseUrl}/${strategy}?stockId=${data.stockId}&userId=${userId}`, null, params);
    
    check(res, {
        'is status 200': (r) => r.status === 200,
        'is status 500 (expected failure)': (r) => r.status === 500,
    });
}

export function teardown(data) {
    // 테스트 종료 후 최종 상태 확인
    const res = http.get(`${baseUrl}/status/${data.stockId}`);
    const body = JSON.parse(res.body);
    console.log(`Test Result: RemainingStock=${body.remainingStock}, ReservationCount=${body.reservationCount}`);
    
    const overReserved = body.reservationCount > stockCount;
    if (overReserved) {
        console.error(`CRITICAL: Over-reservation detected! (${body.reservationCount}/${stockCount})`);
    } else {
        console.log(`SUCCESS: No over-reservation detected.`);
    }
}
