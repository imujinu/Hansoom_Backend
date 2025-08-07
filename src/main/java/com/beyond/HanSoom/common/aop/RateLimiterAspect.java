package com.beyond.HanSoom.common.aop;

import com.beyond.HanSoom.common.annotation.LimitRequestPerTime;
import com.beyond.HanSoom.common.ratelimiter.RateLimiter;
import com.beyond.HanSoom.common.ratelimiter.RedisWithLuaScriptRateLimiter;
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
    private final RedisWithLuaScriptRateLimiter rateLimiter;

    public RateLimiterAspect(@Qualifier("rateLimiter") RedisWithLuaScriptRateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    //todo : 빈 객실 조회 혹은 호텔 조회 시 가로채기
    @Around("execution(* com.beyond.HanSoom.reservation.Reservation.confirm(..))")
    public void interceptor(ProceedingJoinPoint joinPoint) throws  Throwable{
        LimitRequestPerTime limitRequestPerTime = getLimitRequestPerTimeAnnotationFromMethod(joinPoint);
        //LimitRequestPerTime 어노테이션이 안붙어있었을 경우 메서드 그대로 실행

        //dto가 여기 들어 있음 uniqueKey값으로 넘겨주기
//        Object dto = joinPoint.getArgs()[0];

        if(Objects.isNull(limitRequestPerTime)){
            joinPoint.proceed();
        }

        Long uniqueKey = getUniqueKeyFromMethodParameter(joinPoint);
        String redisKey = composeKeyWithUniqueKey("rateLimit", uniqueKey.intValue(), "count");

        rateLimiter.tryApiCall(redisKey, limitRequestPerTime, joinPoint);

    }

    private LimitRequestPerTime getLimitRequestPerTimeAnnotationFromMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // signature안에 method가 들어있고 그 안에서 어노테이션에 접근 가능
        //getAnnotaion은 인자값이 없으면 null 을 리턴한다.
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
