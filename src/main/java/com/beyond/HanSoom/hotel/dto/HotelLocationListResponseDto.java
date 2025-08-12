package com.beyond.HanSoom.hotel.dto;

import com.beyond.HanSoom.hotel.domain.Hotel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    public static HotelLocationListResponseDto fromEntity(Hotel hotel, double distance) {
        return HotelLocationListResponseDto.builder()
                .id(hotel.getId())
                .hotelName(hotel.getHotelName())
                .address(hotel.getAddress())
                .image(hotel.getImage())
//                .price()
                .distance(distance)
                .build();
    }
}
