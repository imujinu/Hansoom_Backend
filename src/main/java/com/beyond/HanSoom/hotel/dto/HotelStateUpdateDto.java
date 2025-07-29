package com.beyond.HanSoom.hotel.dto;

import com.beyond.HanSoom.hotel.domain.HotelState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class HotelStateUpdateDto {
    private Long hotelId;
    private HotelState state;
}
