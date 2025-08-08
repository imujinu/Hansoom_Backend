package com.beyond.HanSoom.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueReservationReqDto {
    private String hotelId;
    private String roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private long maxWaitTime;
    private int maxStock;
    private String userId;


    public QueueReservationReqDto makeDto(ReservationDto dto){
        return QueueReservationReqDto.builder()
                .hotelId(String.valueOf(dto.getHotelId()))
                .roomId(String.valueOf(dto.getRoomId()))
                .checkIn(dto.getCheckIn())
                .checkOut(dto.getCheckOut())
                .maxWaitTime(1800)
                .maxStock(dto.getMaxStock())
                .userId(String.valueOf(dto.getUserId()))
                .build();
    }
}
