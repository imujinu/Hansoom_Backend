package com.beyond.HanSoom.test.concurrency.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OptimisticLockReservationService implements ConcurrencyTestService {
    private final TestReservationService testReservationService;

    @Override
    public void reserve(Long stockId, Long userId) {
        int maxRetry = 50;
        int retryCount = 0;
        
        while (retryCount < maxRetry) {
            try {
                testReservationService.reserveWithNewTransaction(stockId, userId);
                return;
            } catch (org.springframework.orm.ObjectOptimisticLockingFailureException e) {
                retryCount++;
                try {
                    Thread.sleep(50); // 재시도 간격
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new RuntimeException("낙관적 락 재시도 횟수 초과");
    }
}
