package com.beyond.HanSoom.common.service;

import com.beyond.HanSoom.common.dto.ReservationDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class ReservationInventoryService {
    private final RedisTemplate<String,String> redisTemplate;


    public ReservationInventoryService(@Qualifier("reservationList") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public int getInventory(ReservationDto dto) {

        //redis 키값을 호텔 + 객실타입으로 조회
        String key = buildKey(dto.getHotelId(), dto.getRoomId());

        //입력된 모든 날짜를 체크인~체크아웃+1전까지 List에 추가함
        List<String> fields = getFields(dto.getCheckIn(), dto.getCheckOut().minusDays(1));

        // multiGet안에 Collection을 넣으면 알아서 반복문을 돌려준다.
        List<Object> values = redisTemplate.opsForHash().multiGet(key, fields.stream().collect(Collectors.toList()));

        boolean hasExceeded = values.stream()
                .map(obj -> obj == null ? 0L : Integer.parseInt(obj.toString()))  // Object → int 변환
                .anyMatch(stock -> stock >= dto.getMaxStock());

        if (hasExceeded) {
            return 0;
        }

        int minStock = dto.getMaxStock();
        for (int i = 0; i < fields.size(); i++) {
            String strVal = values.get(i) != null ? values.get(i).toString() : null;
            if (strVal != null) {
                int used = Integer.parseInt(strVal);
                int stock = dto.getMaxStock() - used;
                minStock = Integer.min(stock, minStock);
            }
        }

        return minStock;
    }



    public void increaseInventory(ReservationDto dto) {

        String key = buildKey(dto.getHotelId(), dto.getRoomId());
        //예약 가능 여부 검색
        int stock = getInventory(dto);
        if(stock==0){
            throw new IllegalStateException("재고가 부족합니다.");
        }
        // 예약 추가
        List<String> fields = getFields(dto.getCheckIn(), dto.getCheckOut());
        List<Object> values = redisTemplate.opsForHash().multiGet(key, fields.stream().collect(Collectors.toList()));

        // redis 재고 증가
        for (int i = 0; i < fields.size(); i++) {
            LocalDate date = LocalDate.parse(fields.get(i));
            Object obj = values.get(i);
            Long value = obj == null ? 0L : Long.parseLong(obj.toString());
            redisTemplate.opsForHash().increment(key, date.toString(), 1);
        }
    }

    public void cancelInventory(Long hotelId, Long roomId,
                                LocalDate startDate, LocalDate endDate){
        String key = buildKey(hotelId, roomId);
        List<String> fields = getFields(startDate,endDate.minusDays(-1));

        for(int i=0; i<fields.size(); i++){
            LocalDate date = LocalDate.parse(fields.get(i));
            Object value = redisTemplate.opsForHash().get(key,date.toString());
            if(value == null || "0".equals(value.toString())) continue;
            redisTemplate.opsForHash().increment(key, date, -1);
        }

    }

    private String buildKey(Long hotelId, Long roomId) {
        return String.format("hotel:%d:room:%d", hotelId, roomId);
    }

    private static List<String> getFields(LocalDate startDate, LocalDate endDate) {
        List<String> fields = startDate.datesUntil(endDate.plusDays(1))
                .map(date -> date.toString())
                .collect(Collectors.toList());
        return fields;
    }

}
