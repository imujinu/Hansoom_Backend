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
    public static class HotelDto{
        private String hotelName;
        private String hotelAddress;
        private String hotelImage;

    }



    public ReservationResDto fromEntity(Reservation reservation){
        Hotel hotel = reservation.getHotel();
        return ReservationResDto.builder()
                .id(reservation.getId())
                .hotelDto(HotelDto.builder()
                        .hotelName(hotel.getHotelName())
                        .hotelAddress(hotel.getAddress())
                        .hotelImage(hotel.getImage())
                        .build())
                .roomId(reservation.getRoom().getId())
                .checkIn(reservation.getCheckInDate())
                .checkOut(reservation.getCheckOutDate())
                .people(reservation.getPeople())
                .totalPrice(reservation.getPrice())
                .state(reservation.getState())
                .build();
    }

}
