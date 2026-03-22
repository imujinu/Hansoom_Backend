package com.beyond.HanSoom.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class HotelResponseDto {

    private Long hotelId;
    private String hotelName;

    private List<RoomDto> rooms;
    private List<ReviewDto> reviews;
}

