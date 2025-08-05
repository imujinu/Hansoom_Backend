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
    private String queueKey;
    private Long userId;
    private Long timestamp;
    private Long maxWaitTime;
    private Long hotelId;
    private Long roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int maxStock;
}
