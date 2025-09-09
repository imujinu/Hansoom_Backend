package com.beyond.HanSoom.reservation.dto.res;

import com.beyond.HanSoom.reservation.domain.Reservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationResponse {
    private Long id;
    private String reservationId;
    private String status;
    private String message;

    public static ReservationResponse fail(Reservation reservation, String message) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .reservationId(reservation.getUuid())
                .status("FAIL")
                .message(message)
                .build();
    }

    public static ReservationResponse success(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .reservationId(reservation.getUuid())
                .status("SUCCESS")
                .message("결제를 진행합니다.")
                .build();
    }


}