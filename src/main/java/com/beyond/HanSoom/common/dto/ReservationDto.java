package com.beyond.HanSoom.common.dto;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.reservation.dto.req.ReservationReqDto;
import com.beyond.HanSoom.room.domain.Room;
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
    private String roomType;
    private LocalDate startDate;
    private LocalDate endDate;
    private int maxStock;

    public ReservationDto makeDto(Hotel hotel, Room room, LocalDate checkIn, LocalDate checkOut, int maxStock){
        return ReservationDto.builder()
                .hotelId(hotel.getId())
                .roomType(room.getType())
                .startDate(checkIn)
                .endDate(checkOut)
                .maxStock(maxStock)
                .build();
    }
}
