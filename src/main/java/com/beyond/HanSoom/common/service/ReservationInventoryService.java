package com.beyond.HanSoom.common.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ReservationInventoryService {
    private final RedisTemplate<String,String> redisTemplate;


    public ReservationInventoryService(@Qualifier("reservationList") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Map<LocalDate, Integer> getInventoryRange(Long hotelId, String roomType,
                                                     LocalDate startDate, LocalDate endDate) {
        String key = buildKey(hotelId, roomType);

        List<String> fields = startDate.datesUntil(endDate.plusDays(1))
                .map(date -> date.toString())
                .collect(Collectors.toList());

        List<Object> values = redisTemplate.opsForHash().multiGet(key, fields.stream().collect(Collectors.toList()));

        Map<LocalDate, Integer> result = new HashMap<>();
        for (int i = 0; i < fields.size(); i++) {
            LocalDate date = LocalDate.parse(fields.get(i));
            Object value = values.get(i);
            result.put(date, value != null ? Integer.parseInt(value.toString()) : 0);
        }

        return result;
    }

    private String buildKey(Long hotelId, String roomType) {
        return String.format("hotel:%d:room:%s", hotelId, roomType);
    }


}
