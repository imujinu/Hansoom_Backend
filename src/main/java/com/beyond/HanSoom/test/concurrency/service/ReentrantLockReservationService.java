package com.beyond.HanSoom.test.concurrency.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class ReentrantLockReservationService implements ConcurrencyTestService {
    private final TestReservationService testReservationService;
    private final Map<Long, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    @Override
    public void reserve(Long stockId, Long userId) {
        ReentrantLock lock = lockMap.computeIfAbsent(stockId, id -> new ReentrantLock());
        
        lock.lock();
        try {
            testReservationService.reserveWithNewTransaction(stockId, userId);
        } finally {
            lock.unlock();
        }
    }
}
