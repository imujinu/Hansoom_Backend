package com.beyond.HanSoom.reservation.dto.res;


import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.domain.State;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationResDto {
    private UUID id;
    private HotelDto hotelDto;
    private Long roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Long people;
    private Long totalPrice;
    private State state;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public class HotelDto{
        private String hotelName;
        private String hotelAddress;
        private String hotelImage;

    }



    public ReservationResDto fromEntity(Reservation reservation){
        return ReservationResDto.builder()
                .id(reservation.getId())
                .hotelDto(reservation.getHotel().getHotelName(), reservation.getHotel().getAddress(),reservation.getHotel().getImage() )
                .roomId(reservation.getRoom().getId())
                .checkIn(reservation.getCheckInDate())
                .checkOut(reservation.getCheckOutDate())
                .people(reservation.getPeople())
                .totalPrice(reservation.getPrice())
                .state(reservation.getState())
                .build();
    }

}
