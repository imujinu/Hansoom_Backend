package com.beyond.HanSoom.test.payment.service;

import com.beyond.HanSoom.test.payment.domain.PayTestOrderState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * ReentrantLock 전략: JVM 레벨 락으로 직렬화.
 * Phase 2(sleep) 동안 JVM 락은 유지되지만 DB 커넥션은 반환되므로
 * 비관락 대비 커넥션 효율이 높다.
 * 단, 단일 JVM 인스턴스 환경에서만 유효하다 — 수평 확장 시 효과 없음.
 */
@Service
@RequiredArgsConstructor
public class ReentrantPaymentService implements PaymentConcurrencyService {

    private final PayTestOrderService orderService;
    private final Map<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    @Override
    public PayTestOrderState reserve(Long stockId, Long userId, long paymentDelayMs) {
        ReentrantLock lock = lockMap.computeIfAbsent(stockId, id -> new ReentrantLock());

        // Phase 1: JVM 락은 재고 점유까지만 — 직렬화 범위 최소화
        Long orderId;
        lock.lock();
        try {
            orderId = orderService.occupyStockAndCreateOrder(stockId, userId);
        } catch (RuntimeException e) {
            return PayTestOrderState.FAILED;
        } finally {
            lock.unlock();
        }

        // Phase 2: 결제 시뮬레이션 (락 해제 상태, 다른 요청과 병렬 실행)
        try {
            Thread.sleep(paymentDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            orderService.failOrder(orderId, stockId);
            return PayTestOrderState.FAILED;
        }

        // Phase 3: 확정 (별도 트랜잭션)
        orderService.confirmOrder(orderId, stockId);
        return PayTestOrderState.CONFIRMED;
    }
}
