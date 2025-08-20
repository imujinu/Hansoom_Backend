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
    // keys : hotel, room, date
    // ARGV[1] : userId
    // ARGV[2] : TTL
    // ARGV[3] : 재고
    // ARGV[4] : 상태값
    private static final String ADD_TO_QUEUE_SCRIPT =
            // 1. 재고 체크
                    "for i = 1, #KEYS do " +
                    "  local count = redis.call('ZCARD', KEYS[i]) " +
                    "  if tonumber(count or 0) >= tonumber(ARGV[3]) then " +
                    "    return {-2, i} " +  // 재고 없음
                    "  end " +
                    "end " +

                    // 2. 중복 체크
                    "for i = 1, #KEYS do " +
                    "  local memberKey = KEYS[i] .. ':' .. ARGV[1] " +  // userId 기준
                    "  if redis.call('EXISTS', memberKey) == 1 then " +
                    "    return {-1, memberKey} " +  // 이미 존재
                    "  end " +
                    "end " +

                    // 3. 새 멤버 추가 + TTL
                    "for i = 1, #KEYS do " +
                    "  local memberKey = KEYS[i] .. ':' .. ARGV[1] " +
                    "  redis.call('SET', memberKey, ARGV[4], 'EX', ARGV[2]) " +  // ARGV[2]=TTL, ARGV[4]=status
                    "  redis.call('ZADD', KEYS[i], 0, memberKey) " +
                    "end " +

                    // 4. 순위 계산 (ZSET 첫 번째 키 기준)
                    "local members = redis.call('ZRANGE', KEYS[1], 0, -1) " +
                    "local position = 0 " +
                    "for i, member in ipairs(members) do " +
                    "  position = position + 1 " +
                    "  if member == KEYS[1] .. ':' .. ARGV[1] then " +
                    "    break " +
                    "  end " +
                    "end " +
                    "local totalCount = #members " +
                    "return {position, totalCount}";


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
                    "for i=2, #KEYS do " +
                    "local lockOwner = redis.call('GET', KEYS[i]) " +
                    "if lockOwner and lockOwner ~= ARGV[1] then " +
                    "    return {-1, nextUser} " +
                    "end " +
                    "end " +

                    // 락 설정
                    "for i = 2, #KEYS do " +
                    "redis.call('SET', KEYS[i], ARGV[1], 'PX', ARGV[2]) " +
                    " end " +

                    // PENDING을 PROCESSING으로 변경
                    "local userId = string.match(nextUser, '^([^:]+):') " +
                    "redis.call('ZREM', KEYS[1], nextUser) " +
                    "redis.call('ZADD', KEYS[1], ARGV[3], userId .. ':PROCESSING') " +

                    "return {1, userId}";

    public List<Long> addToQueue(QueueReservationReqDto dto) {
        List<String> keys = new ArrayList<>();
        generateQueueKey(dto, keys); // 날짜별 큐 key 생성

        try {
            // ARGV 순서: 1.userId, 2.TTL, 3.maxStock, 4.status
            List<Long> result = redisTemplate.execute(
                    new DefaultRedisScript<>(ADD_TO_QUEUE_SCRIPT, List.class),
                    keys,
                    String.valueOf(dto.getUserId()),
                    String.valueOf(dto.getMaxWaitTime()),  // TTL 초 단위
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
     * 대기열에서 다음 사용자 처리
     */
    public List<Object> processNextInQueue(String queueKey, List<String> lockKey, String myLockValue, long lockTtlMillis) {
        List<String> keys = new ArrayList<>();
        keys.add(queueKey);
        keys.addAll(lockKey); // flatten
        try{
            List<Object> result = redisTemplate.execute(
                    new DefaultRedisScript<>(PROCESS_NEXT_IN_QUEUE_SCRIPT, List.class),
                    keys,  //
                    myLockValue,
                    String.valueOf(lockTtlMillis),
                    String.valueOf(System.currentTimeMillis())
            );

            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }



        return null;
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