package com.beyond.HanSoom.test.concurrency.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NoLockReservationService implements ConcurrencyTestService {
    private final TestReservationService testReservationService;

    @Override
    public void reserve(Long stockId, Long userId) {
        testReservationService.reserveWithNewTransaction(stockId, userId);
    }
}
