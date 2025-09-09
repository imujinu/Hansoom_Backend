package com.beyond.HanSoom.common.dto;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.reservation.dto.req.ReservationReqDto;
import com.beyond.HanSoom.room.domain.Room;
import com.beyond.HanSoom.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDto {
    private Long hotelId;
    private Long roomId;
    private Long userId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int maxStock;

    public ReservationDto makeDto(Hotel hotel, Room room, User user, LocalDate checkIn, LocalDate checkOut, int maxStock){
        return ReservationDto.builder()
                .hotelId(hotel.getId())
                .roomId(room.getId())
                .userId(user.getId())
                .checkIn(checkIn)
                .checkOut(checkOut)
                .maxStock(maxStock)
                .build();
    }
}
