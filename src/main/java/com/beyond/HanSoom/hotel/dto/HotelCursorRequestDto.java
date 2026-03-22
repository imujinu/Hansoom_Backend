package com.beyond.HanSoom.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelCursorRequestDto {
    private Long lastId;         // 마지막 데이터의 ID
    private Integer lastPrice;   // 마지막 데이터의 가격
    private BigDecimal lastRating; // 마지막 데이터의 평점
    @Builder.Default
    private int size = 20;       // 페이지 사이즈
    private String sort;         // 정렬 옵션 (price_asc, rating_desc 등)

    public boolean hasLastId() {
        return lastId != null;
    }
}
