package com.beyond.HanSoom.hotel.dto;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.domain.HotelState;
import com.beyond.HanSoom.hotel.domain.HotelType;
import com.beyond.HanSoom.room.dto.RoomRegisterRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HotelRegisterRequsetDto {
    private String hotelName;
    private String address;
    private String phoneNumber;
    private String describtion;
    private HotelType type;

    private List<RoomRegisterRequestDto> rooms;

    public Hotel toEntity(MultipartFile hotelImage) {
        return Hotel.builder()
                .hotelName(this.hotelName)
                .address(this.address)
                .phoneNumber(this.phoneNumber)
                .describtion(this.describtion)
                .type(this.type)
                .state(HotelState.WAIT)
                .image(hotelImage.getOriginalFilename())
                .build();
    }
}
