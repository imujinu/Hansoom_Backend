package com.beyond.HanSoom.hotel.dto;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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

    @QueryProjection
    public HotelListResponseDto(Long id, String hotelName, String address, String image, int price, BigDecimal rating, int reviewCount) {
        this.id = id;
        this.hotelName = hotelName;
        this.address = address;
        this.image = image;
        this.price = price;
        this.rating = rating;
        this.reviewCount = reviewCount;
    }

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
