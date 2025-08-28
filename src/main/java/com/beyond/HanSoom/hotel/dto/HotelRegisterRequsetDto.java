package com.beyond.HanSoom.hotel.dto;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.domain.HotelState;
import com.beyond.HanSoom.hotel.domain.HotelType;
import com.beyond.HanSoom.hotel.service.GeocoderService;
import com.beyond.HanSoom.room.dto.RoomRegisterRequestDto;
import com.beyond.HanSoom.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HotelRegisterRequsetDto {
    private String hotelName;
    private String address;
    private String phoneNumber;
    private String description;
    private HotelType type;

    private List<RoomRegisterRequestDto> rooms;

    public Hotel toEntity(String hotelImageUrl, GeocoderService.HotelAddressDto coordinate, User user) {
        return Hotel.builder()
                .hotelName(this.hotelName)
                .address(this.address)
                .phoneNumber(this.phoneNumber)
                .description(this.description)
                .type(this.type)
                .state(HotelState.WAIT)
                .image(hotelImageUrl)
                .latitude(coordinate.getLatitude())
                .longitude(coordinate.getLongitude())
                .user(user)
                .build();
    }
}
