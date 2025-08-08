package com.beyond.HanSoom.common.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisDistributedLock {
    private final StringRedisTemplate redisTemplate;


    public RedisDistributedLock (StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryLock(List<String> keys, String value, long expireSeconds) {
        List<String> acquiredKeys = new ArrayList<>();

        for (String key : keys) {
            Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, expireSeconds, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(success)) {
                acquiredKeys.add(key);
            } else {
                // 실패한 경우 지금까지 얻은 락만 해제
                System.out.println("else문 시작");
                for (String acquiredKey : acquiredKeys) {
                    String current = redisTemplate.opsForValue().get(acquiredKey);
                    if (value.equals(current)) {
                        redisTemplate.delete(acquiredKey);
                    }
                }
                return false;
            }
        }
        return true;
    }

    public void releaseLock(List<String> keys, String value) {
        for (String key : keys) {
            String currentValue = redisTemplate.opsForValue().get(key);
            if (value.equals(currentValue)) {
                redisTemplate.delete(key);
            }
        }
    }
}
