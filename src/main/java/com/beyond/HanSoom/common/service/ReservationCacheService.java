package com.beyond.HanSoom.common.service;

import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.dto.res.ReservationCacheResDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component

public class ReservationCacheService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    public ReservationCacheService(@Qualifier("bookingCacheInventory") RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        objectMapper.registerModule(new JavaTimeModule());
    }

    public ReservationCacheResDto getCacheReservation(Long reservationId) throws JsonProcessingException {
        Object cacheReservation = redisTemplate.opsForValue().get(String.valueOf(reservationId));
        if (cacheReservation == null) return null;

        return objectMapper.readValue(cacheReservation.toString(), ReservationCacheResDto.class);
    }

    public void saveCacheReservation(ReservationCacheResDto reservation) throws JsonProcessingException {
        String cacheReservation = objectMapper.writeValueAsString(reservation);
        redisTemplate.opsForValue().set(String.valueOf(reservation.getReservationId()), cacheReservation);
    }
}
