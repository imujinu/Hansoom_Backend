package com.beyond.HanSoom.test.payment.service;

import com.beyond.HanSoom.test.payment.domain.PayTestOrderState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptimisticPaymentService implements PaymentConcurrencyService {

    private final PayTestOrderService orderService;
    private static final int MAX_RETRY = 50;
    private static final long RETRY_INTERVAL_MS = 50;

    @Override
    public PayTestOrderState reserve(Long stockId, Long userId, long paymentDelayMs) {
        Long orderId = null;
        int retryCount = 0;

        // Phase 1: 낙관락 충돌 시 재시도 (트랜잭션 밖에서 루프)
        while (retryCount < MAX_RETRY) {
            try {
                orderId = orderService.occupyStockAndCreateOrder(stockId, userId);
                break;
            } catch (ObjectOptimisticLockingFailureException e) {
                retryCount++;
                try {
                    Thread.sleep(RETRY_INTERVAL_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return PayTestOrderState.FAILED;
                }
            } catch (RuntimeException e) {
                // 재고 부족
                return PayTestOrderState.FAILED;
            }
        }

        if (orderId == null) {
            log.warn("낙관락 재시도 초과: stockId={}, userId={}", stockId, userId);
            return PayTestOrderState.FAILED;
        }

        // Phase 2: 결제 시뮬레이션 (DB 커넥션 미점유)
        try {
            Thread.sleep(paymentDelayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            orderService.failOrder(orderId, stockId);
            return PayTestOrderState.FAILED;
        }

        // Phase 3: 확정
        orderService.confirmOrder(orderId, stockId);
        return PayTestOrderState.CONFIRMED;
    }
}
