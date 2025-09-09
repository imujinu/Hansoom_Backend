package com.beyond.HanSoom.hotel.dto;

import com.beyond.HanSoom.hotel.domain.Hotel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HotelListResponseDto {
    private Long id;
    private String hotelName;
    private String address;
    private String image;
    private int price;
    private BigDecimal rating;
    private int reviewCount;

    public static HotelListResponseDto fromEntity(Hotel hotel, int price) {
        return HotelListResponseDto.builder()
                .id(hotel.getId())
                .hotelName(hotel.getHotelName())
                .address(hotel.getAddress())
                .image(hotel.getImage())
                .price(price)
                .rating(hotel.getHotelReviewSummary().getAverage())
                .reviewCount(hotel.getHotelReviewSummary().getRatingCount())
                .build();
    }
}
