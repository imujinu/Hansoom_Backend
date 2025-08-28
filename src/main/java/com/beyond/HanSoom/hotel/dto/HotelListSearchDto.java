package com.beyond.HanSoom.hotel.dto;

import com.beyond.HanSoom.hotel.domain.HotelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HotelListSearchDto {
    private String hotelName;
    private String address;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int people;
    private int minPrice;
    private int maxPrice;
    @Builder.Default
    private List<HotelType> type = new ArrayList<>();
    private BigDecimal rating;
    private String sortOption;
}
