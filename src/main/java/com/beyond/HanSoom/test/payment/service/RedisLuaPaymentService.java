package com.beyond.HanSoom.test.payment.service;

import com.beyond.HanSoom.test.payment.domain.PayTestOrderState;
import com.beyond.HanSoom.test.payment.domain.PayTestStock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * Redis + Lua 전략: 날짜 단위 없이 단순화된 스크립트로 단일 stockId 단위 재고를 관리.
 * Redis Hash key: pay-test:stock:{stockId}  (DB 8, 프로덕션 key와 네임스페이스 분리)
 * Lua가 조회-예약의 원자성을 보장하므로 JPA 락 불필요.
 */
@Slf4j
@Service
public class RedisLuaPaymentService implements PaymentConcurrencyService {

    private final RedisTemplate<String, String> redisTemplate;
    private final PayTestOrderService orderService;

    public RedisLuaPaymentService(
            @Qualifier("reservationList") RedisTemplate<String, String> redisTemplate,
            PayTestOrderService orderService) {
        this.redisTemplate = redisTemplate;
        this.orderService = orderService;
    }

    // KEYS[1]: pay-test:stock:{stockId}
    // ARGV[1]: userId, ARGV[2]: maxStock, ARGV[3]: status
    private static final DefaultRedisScript<Long> LUA_RESERVE = new DefaultRedisScript<>(
            "local count = redis.call('HLEN', KEYS[1]) " +
            "if tonumber(count or 0) >= tonumber(ARGV[2]) then return -2 end " +
            "if redis.call('HEXISTS', KEYS[1], ARGV[1]) == 1 then return -1 end " +
            "redis.call('HSET', KEYS[1], ARGV[1], ARGV[3]) " +
            "return 0",
            Long.class
    );

    @Override
    public PayTestOrderState reserve(Long stockId, Long userId, long paymentDelayMs) {
        String key = redisKey(stockId);

        // totalStock을 DB에서 조회 (재고 한도로 사용)
        Long totalStock = orderService.getTotalStock(stockId);
        if (totalStock == null) {
            return PayTestOrderState.FAILED;
        }

        // Phase 1: Lua 원자적 실행 — 재고 체크 + 예약 등록
        Long result = redisTemplate.execute(
                LUA_RESERVE,
                List.of(key),
                String.valueOf(userId),
                String.valueOf(totalStock),
                "PENDING"
        );

        if (result == null || result == -2L) {
            return PayTestOrderState.FAILED; // 재고 부족
        }
        if (result == -1L) {
            return PayTestOrderState.FAILED; // 중복 요청
        }

        // DB에 PENDING 주문 기록 (별도 트랜잭션)
        Long orderId = orderService.createPendingOrder(stockId, userId);

        // Phase 2: 결제 시뮬레이션 (DB 커넥션 미점유, Redis 상태 PENDING 유지)
        try {
            Thread.sleep(paymentDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            redisTemplate.opsForHash().delete(key, String.valueOf(userId));
            orderService.failOrderOnly(orderId);
            return PayTestOrderState.FAILED;
        }

        // Phase 3: 확정
        redisTemplate.opsForHash().put(key, String.valueOf(userId), "CONFIRMED");
        orderService.confirmOrder(orderId, stockId);
        return PayTestOrderState.CONFIRMED;
    }

    public void resetRedisKey(Long stockId) {
        redisTemplate.delete(redisKey(stockId));
    }

    public long getRedisOccupied(Long stockId) {
        Long size = redisTemplate.opsForHash().size(redisKey(stockId));
        return size != null ? size : 0L;
    }

    private String redisKey(Long stockId) {
        return "pay-test:stock:" + stockId;
    }
}
