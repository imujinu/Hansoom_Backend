package com.beyond.HanSoom.common.service;

import com.beyond.HanSoom.common.dto.ReservationDto;
import com.beyond.HanSoom.reservation.domain.Reservation;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.beyond.HanSoom.pay.service.PaymentService.generateQueueKey;

@Component
public class ReservationInventoryService {
    private final RedisTemplate<String,String> redisTemplate;


    public ReservationInventoryService(@Qualifier("reservationList") RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public int getInventory(ReservationDto dto) {

        List<String> keys = new ArrayList<>();
        generateQueueKey(dto, keys);

        int minStock = Integer.MAX_VALUE;
        int maxStock = dto.getMaxStock();

        for (String key : keys) {
            Map<Object, Object> members = redisTemplate.opsForHash().entries(key);
            int stock = maxStock - members.size();
            minStock = Math.min(minStock, stock);
        }


        return minStock;
    }

    public static void generateQueueKey(ReservationDto dto,  List<String> keys) {
        for (LocalDate date = dto.getCheckIn(); date.isBefore(dto.getCheckOut()); date = date.plusDays(1)) {
            keys.add(String.format(
                    "queue:hotel:%s:room:%s:date:%s",
                    dto.getHotelId(),
                    dto.getRoomId(),
                    date
            ));
        }
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
