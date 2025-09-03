package com.beyond.HanSoom.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HotelPopularRequestDto {
    private String address;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int people;
}
