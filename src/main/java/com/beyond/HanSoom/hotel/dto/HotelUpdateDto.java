package com.beyond.HanSoom.hotel.dto;

import com.beyond.HanSoom.hotel.domain.HotelType;
import com.beyond.HanSoom.room.dto.RoomUpdateDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HotelUpdateDto {
    private String hotelName;
    private String address;
    private String phoneNumber;
    private String description;
    private HotelType type;
    private List<RoomUpdateDto> rooms;
}
