package com.beyond.HanSoom.common.aop;

import com.beyond.HanSoom.common.annotation.LimitRequestPerTime;
import com.beyond.HanSoom.common.ratelimiter.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@Aspect
@Slf4j
public class RateLimiterAspect {
    private final RateLimiter rateLimiter;

    public RateLimiterAspect(@Qualifier("RedisRateLimiter") RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Around("execution(* com.example.component.*.*(..)))")
    public void interceptor(ProceedingJoinPoint joinPoint) throws  Throwable{
        LimitRequestPerTime limitRequestPerTime = getLimitRequestPerTimeAnnotationFromMethod(joinPoint);
        if(Objects.isNull(limitRequestPerTime)){
            joinPoint.proceed();
            return;
        }
    }

    private LimitRequestPerTime getLimitRequestPerTimeAnnotationFromMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        LimitRequestPerTime limitRequestPerTime = method.getAnnotation(LimitRequestPerTime.class);
        return limitRequestPerTime;
    }
    private Long getUniqueKeyFromMethodParameter(ProceedingJoinPoint joinPoint) {
        List<Object> parameters = Arrays.asList(joinPoint.getArgs());
        return (Long) parameters.get(0);
    }

    private String composeKeyWithUniqueKey(String prefix, int uniqueId,String suffix) {
        StringBuffer stringBuffer = new StringBuffer();
        return stringBuffer
                .append(prefix).append(":")
                .append(uniqueId).append(":")
                .append(suffix)
                .toString();
    }

}
