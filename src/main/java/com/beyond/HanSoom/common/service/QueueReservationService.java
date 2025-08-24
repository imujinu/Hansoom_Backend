package com.beyond.HanSoom.common.service;

import com.beyond.HanSoom.common.dto.QueueReservationReqDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
//    -- KEYS: 호텔:룸:날짜
//-- ARGV[1]: userId
//-- ARGV[2]: maxStock
//-- ARGV[3]: status
    private static final String ADD_TO_QUEUE_SCRIPT =
//                1. 재고 조회
            "for i = 1, #KEYS do " +
                    "  local count = redis.call('HLEN', KEYS[i]) " +
                    "  if tonumber(count or 0) >= tonumber(ARGV[2]) then " +
                    "    return {-2, i} " +
                    "  end " +
                    "end " +

                    //2. 중복 체크
                    "for i = 1, #KEYS do " +
                    "  if redis.call('HEXISTS', KEYS[i], ARGV[1]) == 1 then " +
                    "    return {-1} " +
                    "  end " +
                    "end " +

                    // 3. 예약 추가
                    "for i = 1, #KEYS do " +
                    "  redis.call('HSET', KEYS[i], ARGV[1], ARGV[3]) " +
                    "end " +

                    "return {0}";

    public List<Long> addToQueue(QueueReservationReqDto dto) {
        List<String> keys = new ArrayList<>();
        generateQueueKey(dto, keys); // 날짜별 큐 key 생성

//    -- KEYS: 호텔:룸:날짜
//-- ARGV[1]: userId
//-- ARGV[2]: maxStock
//-- ARGV[3]: status
        try {

            List<Long> result = redisTemplate.execute(
                    new DefaultRedisScript<>(ADD_TO_QUEUE_SCRIPT, List.class),
                    keys,
                    String.valueOf(dto.getUserId()),
                    String.valueOf(dto.getMaxStock()),     // 재고
                    "PENDING"                              // 상태
            );

            return result;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Queue 추가 중 에러 발생!!");
        }
    }

    public static void generateQueueKey(QueueReservationReqDto dto, List<String> keys) {
        for (LocalDate date = dto.getCheckIn(); date.isBefore(dto.getCheckOut()); date = date.plusDays(1)) {
            keys.add(String.format(
                    "queue:hotel:%s:room:%s:date:%s",
                    dto.getHotelId(),
                    dto.getRoomId(),
                    date
            ));
        }
    }

    /**
     * 결제 완료 후 상태 업데이트
     */
    public void updateStatus(String queueKey, String userId, String toStatus) {
        redisTemplate.opsForHash().put(queueKey, userId, toStatus);
    }

    public void setTtl(String queueKey, LocalDate date) {
        // 현재 시간
        LocalDateTime now = LocalDateTime.now();
        // 만료할 날짜의 자정 기준으로 계산
        LocalDateTime expireDateTime = date.atStartOfDay();
        Duration duration = Duration.between(now, expireDateTime);
        if (!duration.isNegative() && !duration.isZero()) {
            redisTemplate.expire(queueKey, duration);
        } else {
            // 이미 지난 날짜면 바로 만료
            redisTemplate.expire(queueKey, Duration.ZERO);
        }
    }
    public void removeMember(String queueKey, String userId) {
        redisTemplate.opsForHash().delete(queueKey, userId);
    }
}