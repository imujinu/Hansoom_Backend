package com.beyond.HanSoom.reservation.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {
    private String reservationId;
    private String status;
    private String message;

    public static ReservationResponse fail(String message) {
        return ReservationResponse.builder()
                .reservationId(null)
                .status("FAIL")
                .message(message)
                .build();
    }

    public static ReservationResponse success(String reservationId) {
        return ReservationResponse.builder()
                .reservationId(reservationId)
                .status("SUCCESS")
                .message("결제를 진행합니다.")
                .build();
    }


}