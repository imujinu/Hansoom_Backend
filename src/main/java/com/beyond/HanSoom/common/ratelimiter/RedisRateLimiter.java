package com.beyond.HanSoom.common.ratelimiter;

import com.beyond.HanSoom.common.annotation.LimitRequestPerTime;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Slf4j
public class RedisRateLimiter implements RateLimiter{
    private final RedisTemplate<String, Serializable> redisTemplate;

    public RedisRateLimiter(@Qualifier("rateLimiter") RedisTemplate<String, Serializable> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    @Override
    public void tryApiCall(String key, LimitRequestPerTime limitRequestPerTime, ProceedingJoinPoint joinPoint) throws Throwable {
        Long previousCount = (Long) redisTemplate.opsForValue().get(key);

        if(previousCount != null && previousCount.intValue() > limitRequestPerTime.count()){
            throw new RuntimeException("1분당 호출 수 초과");
        }

        redisTemplate.execute(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> operations) throws DataAccessException {
                try{
                    operations.multi();
                    if(previousCount == null){
                        redisTemplate.opsForValue().set(key, 0 , limitRequestPerTime.ttl(), limitRequestPerTime.ttlTimeUnit());
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    operations.discard();
                    throw e;
                }
                return  operations.exec();
            }
        });
        joinPoint.proceed();

    }
}
