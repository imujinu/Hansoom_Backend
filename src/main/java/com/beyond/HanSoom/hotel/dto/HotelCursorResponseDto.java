package com.beyond.HanSoom.hotel.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelCursorResponseDto<T> {
    private List<T> content;
    private boolean hasNext;
    private Long lastId;
    private Integer lastPrice;
    private java.math.BigDecimal lastRating;

    public static <T> HotelCursorResponseDto<T> of(List<T> content, boolean hasNext, Long lastId, Integer lastPrice, java.math.BigDecimal lastRating) {
        return new HotelCursorResponseDto<>(content, hasNext, lastId, lastPrice, lastRating);
    }
}
