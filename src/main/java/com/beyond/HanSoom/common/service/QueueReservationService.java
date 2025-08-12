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
    // userId
    // 생성 시간
    // 만료시간
    // 최대재고
    // 상태
    private static final String ADD_TO_QUEUE_SCRIPT =


            // 1. 재고 체크 (tonumber(ARGV[4]) or 0)
            "local maxStock = tonumber(ARGV[4]) or 50 " +
            "for i = 1, #KEYS do " +
                    "  local count = redis.call('ZCARD', KEYS[i]) " +
                    "  if tonumber(count or 0) >= maxStock then " +
                    "    return {-2, i} " +
                    "  end " +
                    "end " +

                    "  local now = tonumber(ARGV[2]) or 0 " +
                    "local maxWaitMillis = (tonumber(ARGV[3]) or 0) * 1000 " +
                    "local expireAt = now + maxWaitMillis " +

                    "for i = 1, #KEYS do " +
                    "  local exist = redis.call('ZSCORE', KEYS[i], ARGV[1] .. ':' .. ARGV[5]) " +
                    "  if exist ~= false and exist ~= nil then " +
                    "    local existNum = tonumber(exist) " +
                    "    if existNum ~= nil and existNum > now then " +
                    " local pos = redis.call('ZRANK', KEYS[i], ARGV[1] .. ':' .. ARGV[5]) " +
                    " if pos == 0 then " +
                    "   return {1, redis.call('ZCARD', KEYS[i]) } " +
                    " end " +
                    "      return {-1, exist} " +
                    "       end " +
                    "   end " +
                    "end " +

                    "for i = 1, #KEYS do " +
                    "  redis.call('ZADD', KEYS[i], expireAt, ARGV[1] .. ':' .. ARGV[5]) " +
                    "end " +

                    "local position = redis.call('ZRANK', KEYS[1], ARGV[1] .. ':' .. ARGV[5]) " +
                    "if not position then position = -1 end " +
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
        LocalDate start = LocalDate.parse(dto.getCheckIn().toString());
        LocalDate end = LocalDate.parse(dto.getCheckOut().toString());

        generateQueueKey(dto, start, end, keys);

        try{
            List<Long> list = redisTemplate.execute(
                    new DefaultRedisScript<>(ADD_TO_QUEUE_SCRIPT, List.class),
                    keys,
                    dto.getUserId(),
                    String.valueOf(System.currentTimeMillis()),
                    String.valueOf(dto.getMaxWaitTime()),
                    String.valueOf(dto.getMaxStock()),
                    "PENDING" // 초기 상태 (실제로는 스크립트에서 하드코딩)
            );
            return list;


        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("여기서 에러가 터짐!!");
        }



    }

    public static void generateQueueKey(QueueReservationReqDto dto, LocalDate start, LocalDate end, List<String> keys) {
        for (LocalDate date = start; date.isBefore(end); date = date.plusDays(1)) {
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