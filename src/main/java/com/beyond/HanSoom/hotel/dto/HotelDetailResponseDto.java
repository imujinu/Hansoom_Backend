package com.beyond.HanSoom.hotel.dto;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.domain.HotelType;
import com.beyond.HanSoom.room.dto.RoomDetailResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HotelDetailResponseDto {
    private String hotelName;
    private String address;
    private String phoneNumber;
    private String image;
    private String description;
    private HotelType type;
    private double latitude;
    private double longitude;
    private List<RoomDetailResponseDto> roomDetailResponseDtoList;

    public static HotelDetailResponseDto fromEntity(Hotel hotel, List<RoomDetailResponseDto> dto) {
        return HotelDetailResponseDto.builder()
                .hotelName(hotel.getHotelName())
                .address(hotel.getAddress())
                .phoneNumber(hotel.getPhoneNumber())
                .image(hotel.getImage())
                .description(hotel.getDescription())
                .type(hotel.getType())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .roomDetailResponseDtoList(dto)
                .build();
    }
}
