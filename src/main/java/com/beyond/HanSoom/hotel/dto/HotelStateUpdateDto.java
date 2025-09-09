package com.beyond.HanSoom.hotel.dto;

import com.beyond.HanSoom.hotel.domain.HotelState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HotelStateUpdateDto {
    private Long hotelId;
    private HotelState state;
}
