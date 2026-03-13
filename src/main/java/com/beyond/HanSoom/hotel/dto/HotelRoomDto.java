package com.beyond.HanSoom.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class HotelRoomDto {

    private Long hotelId;
    private String hotelName;
    private Long roomId;
    private int price;

}
