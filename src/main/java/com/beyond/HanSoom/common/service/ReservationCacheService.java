package com.beyond.HanSoom.common.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component

public class ReservationCacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    public ReservationCacheService(@Qualifier("bookingCacheInventory") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
}
