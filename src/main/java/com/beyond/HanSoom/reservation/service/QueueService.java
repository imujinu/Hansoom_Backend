package com.beyond.HanSoom.reservation.service;

import com.beyond.HanSoom.reservation.dto.req.QueueReqDto;
import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class QueueService {

    private final RedisTemplate<String, String> redisTemplate;
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final int maxQueueSize = 5;
    private final long userTtlSeconds = 600; // 개별 유저 TTL
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final UserRepository userRepository;

    public QueueService(@Qualifier("queueRedisTemplate") RedisTemplate<String, String> redisTemplate, UserRepository userRepository) {
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;

        // 1초마다 TTL 만료 체크 및 SSE 브로드캐스트
        scheduler.scheduleAtFixedRate(() -> {
            broadcastAllQueues();
            removeExpiredUsers();
        }, 0, 1, TimeUnit.SECONDS);
    }

    /** 대기열 진입 */
    public boolean enterQueue(QueueReqDto dto) {
        String queueKey = buildQueueKey(dto);
        Long size = redisTemplate.opsForZSet().zCard(queueKey);
        System.out.println("queueSize : " + size);

        if (size >= maxQueueSize) {
            System.out.println("queue등록 실패");
            return false;
        }
        String userId = dto.getUserId();
        // ZSET에 등록 (순서 관리)
        redisTemplate.opsForZSet().add(queueKey, userId, System.currentTimeMillis());

        // 개별 TTL용 String key
        String ttlKey = buildUserTtlKey(dto);
        redisTemplate.opsForValue().set(ttlKey, "1", userTtlSeconds, TimeUnit.SECONDS);
        System.out.println("queue 등록 완료");
        return true;
    }

    private String getUserId() {
        String email= SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("존재하지 않는 유저입니다."));
        Long userId = user.getId();
        return String.valueOf(userId);
    }

    /** 대기열 탈출 */
    public void leaveQueue(QueueReqDto dto) {
        System.out.println("삭제 로직 동작");
        String queueKey = buildQueueKey(dto);
        String userId = dto.getUserId();
        redisTemplate.opsForZSet().remove(queueKey, userId);

        String ttlKey = buildUserTtlKey(dto);
        redisTemplate.delete(ttlKey);

        emitters.remove(userId + ":" + queueKey);
        broadcastAllQueues();
    }

    /** SSE 구독 등록 */
    public SseEmitter registerEmitter(QueueReqDto dto) {
        String queueKey = buildQueueKey(dto);
        String userId = dto.getUserId();
        String emitterKey = userId + ":" + queueKey;

        SseEmitter emitter = new SseEmitter(300 * 1000L);
        emitters.put(emitterKey, emitter);

        emitter.onCompletion(() -> emitters.remove(emitterKey));
        emitter.onTimeout(() -> emitters.remove(emitterKey));

        return emitter;
    }

    /** TTL 만료된 유저 제거 */
    private void removeExpiredUsers() {
        for (String key : emitters.keySet()) {
            String[] parts = key.split(":", 2);
            String userId = parts[0];
            String queueKey = parts[1];
            String ttlKey = queueKey + ":" + userId;
            Boolean exists = redisTemplate.hasKey(ttlKey);
            if (exists == null || !exists) {
                redisTemplate.opsForZSet().remove(queueKey, userId);
                emitters.remove(key);
            }
        }
    }
    private void removeUser(){
        String userId = getUserId();
        String queueKey = null;

        for(String key : emitters.keySet()){
            if(key.startsWith(userId + "")){
                queueKey = key.split(":",2)[1];
                break;
            }
        }

        emitters.remove(userId);
        redisTemplate.opsForZSet().remove(queueKey,userId);
    }

    /** SSE 1초 브로드캐스트 */
    private void broadcastAllQueues() {
        Map<String, List<String>> cache = new HashMap<>();
        for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
            String emitterKey = entry.getKey();
            SseEmitter emitter = entry.getValue();

            String[] parts = emitterKey.split(":", 2);
            String userId = parts[0];
            String queueKey = parts[1];



            try {
                Set<String> zset = redisTemplate.opsForZSet().range(queueKey, 0, -1);
                List<String> queue = zset != null ? new ArrayList<>(zset) : new ArrayList<>();
                Long rank = redisTemplate.opsForZSet().rank(queueKey, userId);
                Map<String, Object> payload = new HashMap<>();
                payload.put("queueSize", queue.size());
                payload.put("myRank", rank != null ? rank + 1 : queue.size() + 1);
                payload.put("inQueue", rank != null && rank < maxQueueSize);

                emitter.send(SseEmitter.event().name("queue-update").data(payload));
            } catch (Exception e) {
                emitters.remove(emitterKey);
            }
        }
    }

    /** ZSET key 생성 */
    private String buildQueueKey(QueueReqDto dto) {
        return String.format("queue:hotel:%s:room:%s:date:%s",
                dto.getHotelId(), dto.getRoomId(), dto.getCheckInDate().format(DateTimeFormatter.BASIC_ISO_DATE));
    }

    /** 개별 TTL key 생성 */
    private String buildUserTtlKey(QueueReqDto dto) {
        return buildQueueKey(dto) + ":" + dto.getUserId();
    }
}


