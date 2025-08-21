package com.beyond.HanSoom.reservation.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class QueueService {

    private final RedisTemplate<String, String> redisTemplate;
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final int maxQueueSize = 200; // 동시에 접속 가능한 최대 사용자 수

    public QueueService(@Qualifier("queueRedisTemplate")RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryEnterQueue(String queueKey, String userId) {
        Long currentSize = redisTemplate.opsForHash().size(queueKey);
        if (currentSize >= maxQueueSize) return false;

        // HSET에 등록 (value는 timestamp)
        redisTemplate.opsForHash().put(queueKey, userId, String.valueOf(System.currentTimeMillis()));
        broadcastQueue(queueKey);
        return true;
    }

    public void leaveQueue(String queueKey, String userId) {
        redisTemplate.opsForHash().delete(queueKey, userId);
        broadcastQueue(queueKey);
    }

    public Map<String, Long> getSortedQueue(String queueKey) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(queueKey);
        return entries.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> (String) e.getKey(),
                        e -> Long.parseLong((String) e.getValue())
                ))
                .entrySet().stream()
                .sorted(Comparator.comparing(e -> Long.parseLong(e.getValue().toString())))
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new
                ));
    }

    public void registerEmitter(SseEmitter emitter) {
        emitters.add(emitter);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
    }

    public void broadcastQueue(String queueKey) {
        Map<String, Long> sorted = getSortedQueue(queueKey);
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(sorted);
            } catch (Exception e) {
                emitters.remove(emitter);
            }
        }
    }
}
