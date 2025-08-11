package com.beyond.HanSoom.common.service;

import com.beyond.HanSoom.reservation.domain.Reservation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component

public class ReservationCacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    public ReservationCacheService(@Qualifier("bookingCacheInventory") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Optional<Reservation> getCacheReservation(Long reservationId) throws JsonProcessingException {

        Object cacheReservation = redisTemplate.opsForValue().get(reservationId);
        if(cacheReservation==null){
            return Optional.empty();
        }else{

        ObjectMapper objectMapper = new ObjectMapper();

        Reservation reservation1 = objectMapper.readValue(cacheReservation.toString(), Reservation.class);
        return Optional.of(reservation1);
        }
    }

    public void saveCacheReservation(Reservation reservation) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String cacheReservation = objectMapper.writeValueAsString(reservation);
        redisTemplate.opsForValue().set(String.valueOf(reservation.getId()), cacheReservation);
    }
}
