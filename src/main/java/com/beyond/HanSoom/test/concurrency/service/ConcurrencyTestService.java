package com.beyond.HanSoom.test.concurrency.service;

public interface ConcurrencyTestService {
    void reserve(Long stockId, Long userId);
}
