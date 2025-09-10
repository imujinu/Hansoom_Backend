package com.beyond.HanSoom.reservation.dto.res;

import com.beyond.HanSoom.reservation.domain.Reservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationCompleteResDto {
    private Long reservationId;


    public ReservationCompleteResDto fromEntity(Long reservationId) {
        return ReservationCompleteResDto.builder()
                .reservationId(reservationId)
                .build();
    }
}
