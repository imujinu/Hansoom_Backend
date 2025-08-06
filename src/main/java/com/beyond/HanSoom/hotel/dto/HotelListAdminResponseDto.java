package com.beyond.HanSoom.hotel.dto;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.domain.HotelState;
import com.beyond.HanSoom.hotel.domain.HotelType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HotelListAdminResponseDto {
    private Long id;
    private String hotelName;
    private String address;
    private String hostName;
    private HotelState state;
    private LocalDateTime createdTime;

    public static HotelListAdminResponseDto fromEntity(Hotel hotel) {
        return HotelListAdminResponseDto.builder()
                .id(hotel.getId())
                .hotelName(hotel.getHotelName())
                .address(hotel.getAddress())
                .state(hotel.getState())
                .createdTime(hotel.getCreatedTime())
                .hostName(hotel.getUser().getName())
                .build();
    }
}
