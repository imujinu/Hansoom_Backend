package com.beyond.HanSoom.room.domain;

import com.beyond.HanSoom.hotel.domain.HotelState;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Builder
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private int roomCount;
    private String roomOption1;
    private String roomOption2;
    private String describtion;
    private int weekPrice;
    private int weekendPrice;
    private int standardPeople;
    private int maximumPeople;
    private int extraFee;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private HotelState state;
}
