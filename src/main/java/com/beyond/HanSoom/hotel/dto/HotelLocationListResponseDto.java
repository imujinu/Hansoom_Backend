package com.beyond.HanSoom.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HotelLocationListResponseDto {
    private Long id;
    private String hotelName;
    private String address;
    private String image;
    private int price;
    private double distance;
    private double latitude;
    private double longitude;
    private BigDecimal rating;
    private int reviewCount;
}
