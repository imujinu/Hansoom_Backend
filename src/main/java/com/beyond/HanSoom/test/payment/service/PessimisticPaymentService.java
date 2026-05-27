package com.beyond.HanSoom.test.payment.service;

import com.beyond.HanSoom.test.payment.domain.PayTestOrderState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 비관락 전략 (락 범위 최소화):
 * Phase 1에서만 SELECT FOR UPDATE를 사용하고 즉시 커밋 → 락 해제.
 * Phase 2(결제 대기) 동안 DB 커넥션과 행 락을 점유하지 않으므로
 * Redis+Lua, ReentrantLock과 동등한 처리량을 기대할 수 있다.
 */
@Service
@RequiredArgsConstructor
public class PessimisticPaymentService implements PaymentConcurrencyService {

    private final PayTestOrderService orderService;

    @Override
    public PayTestOrderState reserve(Long stockId, Long userId, long paymentDelayMs) {
        // Phase 1: SELECT FOR UPDATE — 재고 점유 후 REQUIRES_NEW 커밋으로 즉시 락 해제
        Long orderId;
        try {
            orderId = orderService.occupyStockWithPessimisticLockAndCreateOrder(stockId, userId);
        } catch (RuntimeException e) {
            return PayTestOrderState.FAILED;
        }

        // Phase 2: 결제 시뮬레이션 (락 해제 상태, Phase 2~3 병렬 실행)
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
