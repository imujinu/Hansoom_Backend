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
    private Long hostId;
    private Long guestId;

    public ReservationCompleteResDto fromEntity(Long reservationId, Long hostId, Long guestId) {
        return ReservationCompleteResDto.builder()
                .reservationId(reservationId)
                .hostId(hostId)
                .guestId(guestId)
                .build();
    }
}
