package com.beyond.HanSoom.reservation.dto.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequest {
    private String userId;
    private String hotelId;
    private String roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
}
