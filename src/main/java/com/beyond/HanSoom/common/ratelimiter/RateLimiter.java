package com.beyond.HanSoom.common.ratelimiter;

import com.beyond.HanSoom.common.annotation.LimitRequestPerTime;
import org.aspectj.lang.ProceedingJoinPoint;

public interface RateLimiter {
    void tryApiCall(String key, LimitRequestPerTime limitRequestPerTime, ProceedingJoinPoint joinPoint) throws Throwable;
}
