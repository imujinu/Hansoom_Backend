package com.beyond.HanSoom.common.aop;

import com.beyond.HanSoom.reservation.service.QueueService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class QueueAspect {

    private final QueueService queueService;

    public QueueAspect(QueueService queueService) {
        this.queueService = queueService;
    }

    @Before("@annotation(QueueLimit) && args(queueKey,userId,..)")
    public void checkQueue(JoinPoint joinPoint, String queueKey, String userId) {
        boolean entered = queueService.tryEnterQueue(queueKey, userId);
        if (!entered) {
            throw new RuntimeException("대기열 초과: 잠시 후 다시 시도하세요.");
        }
    }
}

