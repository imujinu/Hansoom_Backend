package com.beyond.HanSoom.common.aop;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Aspect
@Component
public class ReservationQueueAspect {

    private final RedisTemplate<String, String> redisTemplate;
    private final int maxQueueSize = 200; // 최대 대기열 수

    public ReservationQueueAspect(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Before("@annotation(ReservationQueue)")
    public void beforeReservation(JoinPoint joinPoint) throws Throwable {
        String userId = getUserIdFromJoinPoint(joinPoint); // 컨트롤러 매개변수나 세션에서 userId 추출
        String queueKey = "reservation:queue";

        // HSET 전체 조회
        Map<Object, Object> members = redisTemplate.opsForHash().entries(queueKey);

        // 이미 큐에 있는지 확인
        if (!members.containsKey(userId)) {
            if (members.size() >= maxQueueSize) {
                throw new RuntimeException("대기열이 가득 찼습니다.");
            }
            // 순서 기록
            redisTemplate.opsForHash().put(queueKey, userId, String.valueOf(System.currentTimeMillis()));
        }

        // 순위 계산
        Map<Object, Object> sortedMembers = redisTemplate.opsForHash().entries(queueKey)
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(e -> Long.parseLong(e.getValue().toString())))
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new
                ));

        int rank = 1;
        for (Object key : sortedMembers.keySet()) {
            if (key.equals(userId)) break;
            rank++;
        }

        System.out.println("현재 대기 순위: " + rank);
    }

    private String getUserIdFromJoinPoint(JoinPoint joinPoint) {
        // 컨트롤러 매개변수에서 userId 가져오는 로직 구현
        return "exampleUserId";
    }
}
