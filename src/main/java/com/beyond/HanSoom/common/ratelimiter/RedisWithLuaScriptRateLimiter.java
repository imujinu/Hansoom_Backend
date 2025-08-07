package com.beyond.HanSoom.common.ratelimiter;

import com.beyond.HanSoom.common.annotation.LimitRequestPerTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Component("rateLimiter")
@Configuration
@Slf4j
@RequiredArgsConstructor
public class RedisWithLuaScriptRateLimiter implements RateLimiter{
    private final RedisTemplate<String, Serializable> redisTemplate;


    @Bean
    public DefaultRedisScript<Long> rateLimitScript() {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(
                "local key = KEYS[1] " +
                        "local limitCount = tonumber(ARGV[1]) " +
                        "local limitTime = tonumber(ARGV[2]) " +
                        "local current = tonumber(redis.call('get', key) or '0') " +
                        "if current + 1 > limitCount then " +
                        "  return 0 " +
                        "else " +
                        "  redis.call('INCRBY', key, '1') " +
                        "  redis.call('expire', key, limitTime) " +
                        "  return current + 1 " +
                        "end"
        );
        redisScript.setResultType(Long.class);
        return redisScript;
    }


    @Override
    public void tryApiCall(String key, LimitRequestPerTime limitRequestPerTime, ProceedingJoinPoint joinPoint) throws Throwable {
       Long callCounter = redisTemplate.execute(rateLimitScript(), Collections.singletonList(key), limitRequestPerTime.count(), TimeUnit.SECONDS.convert(limitRequestPerTime.ttl(), limitRequestPerTime.ttlTimeUnit()));

       if(callCounter == null){
           log.error("호출수 조회 실패");
           joinPoint.proceed();
           return;
       }
       if(callCounter.intValue() !=0 && callCounter.intValue() <= limitRequestPerTime.count()){
           joinPoint.proceed();
           return;
       }
       throw new RuntimeException("1분당 호출 수 초과");

    }
}
