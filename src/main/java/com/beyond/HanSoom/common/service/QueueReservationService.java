package com.beyond.HanSoom.common.service;

import com.beyond.HanSoom.common.dto.QueueReservationReqDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class QueueReservationService {

    private final RedisTemplate<String, String> redisTemplate;

    public QueueReservationService(@Qualifier("reservationList") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Lua 스크립트:
     * 1. 날짜별 재고 체크
     * 2. 대기열 등록 (userId:status 형태로)
     * 3. 순위 반환
     */
    private static final String ADD_TO_QUEUE_SCRIPT =
            // 1. 재고 체크
            "for i = 1, #KEYS do " +
                    "    local count = redis.call('ZCARD', KEYS[i]) " +
                    "    if tonumber(count) >= tonumber(ARGV[5]) then " +
                    "        return {-2, i} " +
                    "    end " +
                    "end " +

                    // 2. 중복 체크
                    "for i = 1, #KEYS do " +
                    "    local exist = redis.call('ZSCORE', KEYS[i], ARGV[1] .. ':PENDING') " +
                    "    if exist then " +
                    "        return {-1, exist} " +
                    "    end " +
                    "end " +

                    // 3. 대기열 등록 (userId:PENDING 형태로)
                    "for i = 1, #KEYS do " +
                    "    redis.call('ZADD', KEYS[i], ARGV[2], ARGV[1] .. ':PENDING') " +
                    "    redis.call('EXPIRE', KEYS[i], ARGV[3]) " +
                    "end " +

                    // 4. 첫 번째 날짜 키 기준 순위 및 총 인원 반환
                    "local position = redis.call('ZRANK', KEYS[1], ARGV[1] .. ':PENDING') " +
                    "local totalCount = redis.call('ZCARD', KEYS[1]) " +
                    "return {position + 1, totalCount}";

    /**
     * Lua 스크립트:
     * 1. 대기열 맨 앞 사람 꺼내기
     * 2. 락 검사
     * 3. PENDING을 PROCESSING으로 변경
     */
    private static final String PROCESS_NEXT_IN_QUEUE_SCRIPT =
            // 맨 앞 사람 꺼냄
            "local nextUser = redis.call('ZRANGE', KEYS[1], 0, 0)[1] " +
                    "if not nextUser then " +
                    "    return {0, nil} " +
                    "end " +

                    // 락 상태 확인
                    "local lockOwner = redis.call('GET', KEYS[2]) " +
                    "if lockOwner and lockOwner ~= ARGV[1] then " +
                    "    return {-1, nextUser} " +
                    "end " +

                    // 락 설정
                    "redis.call('SET', KEYS[2], ARGV[1], 'PX', ARGV[2]) " +

                    // PENDING을 PROCESSING으로 변경
                    "local userId = string.match(nextUser, '^([^:]+):') " +
                    "redis.call('ZREM', KEYS[1], nextUser) " +
                    "redis.call('ZADD', KEYS[1], ARGV[3], userId .. ':PROCESSING') " +

                    "return {1, userId}";

    public List<Long> addToQueue(QueueReservationReqDto dto) {
        List<String> keys = new ArrayList<>();
        LocalDate start = LocalDate.parse(dto.getCheckIn().toString());
        LocalDate end = LocalDate.parse(dto.getCheckOut().toString());

        for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
            keys.add(String.format(
                    "queue:hotel:%s:room:%s:date:%s",
                    dto.getHotelId(),
                    dto.getRoomId(),
                    date
            ));
        }

        return redisTemplate.execute(
                new DefaultRedisScript<>(ADD_TO_QUEUE_SCRIPT, List.class),
                keys,
                dto.getUserId(),
                String.valueOf(dto.getTimestamp()),
                String.valueOf(dto.getMaxWaitTime()),
                "PENDING", // 초기 상태 (실제로는 스크립트에서 하드코딩)
                String.valueOf(dto.getMaxStock())
        );
    }

    /**
     * 대기열에서 다음 사용자 처리
     */
    public List<Object> processNextInQueue(String queueKey, String lockKey, String myLockValue, long lockTtlMillis) {
        return redisTemplate.execute(
                new DefaultRedisScript<>(PROCESS_NEXT_IN_QUEUE_SCRIPT, List.class),
                List.of(queueKey, lockKey),
                myLockValue,
                String.valueOf(lockTtlMillis),
                String.valueOf(System.currentTimeMillis()) // PROCESSING 상태의 새로운 스코어
        );
    }

    /**
     * 결제 완료 후 상태 업데이트
     */
    public void updateStatus(String queueKey, String userId, String fromStatus, String toStatus) {
        String oldMember = userId + ":" + fromStatus;
        String newMember = userId + ":" + toStatus;

        Double score = redisTemplate.opsForZSet().score(queueKey, oldMember);
        if (score != null) {
            redisTemplate.opsForZSet().remove(queueKey, oldMember);
            redisTemplate.opsForZSet().add(queueKey, newMember, score);
        }
    }
}