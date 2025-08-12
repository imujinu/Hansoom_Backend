package com.beyond.HanSoom.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

@Target({ElementType.TYPE, ElementType.METHOD})
//클래스, 인터페이스, enum, 애너테이션에 사용할 수 있게 해줌
// Method에서 사용할 수 있게 해줌
@Retention(RetentionPolicy.RUNTIME)
// 실행중에도 생명 주기가 유지 됨
public @interface LimitRequestPerTime {
    //unique key prefix
    String prefix() default "";
    // 호출제한시간설정
    int ttl() default 1;
    // 호출제한 시간 단위
    TimeUnit ttlTimeUnit();
    // 분당 호출 제한 카운트
    int count();
}
