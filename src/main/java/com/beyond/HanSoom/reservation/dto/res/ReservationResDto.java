package com.beyond.HanSoom.reservation.dto.res;


import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.domain.State;
import com.beyond.HanSoom.review.domain.Review;
import com.beyond.HanSoom.room.domain.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationResDto {
    private Long id;
    private Long hotelId;
    private Long chatRoomId;
    private String hotelName;
    private String hotelImage;
    private BigDecimal hotelRating;
    private String roomType;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Long guests;
    private Long totalPrice;
    private String status;
    private LocalDate bookingDate;
    private String reservationNumber;
    private String address;

    public ReservationResDto fromEntity(Reservation reservation, BigDecimal hotelRating, String status, Long chatRoomId){
        Hotel hotel = reservation.getHotel();
        Room room = reservation.getRoom();

        return ReservationResDto.builder()
                .id(reservation.getId())
                .hotelId(hotel.getId())
                .chatRoomId(chatRoomId)
                .hotelName(hotel.getHotelName())
                .hotelImage(hotel.getImage())
                .hotelRating(hotelRating)
                .roomType(room.getType())
                .checkIn(reservation.getCheckInDate())
                .checkOut(reservation.getCheckOutDate())
                .guests(reservation.getPeople())
                .totalPrice(reservation.getPrice())
                .status(status)
                .bookingDate(reservation.getReservationDate().toLocalDate())
                .reservationNumber(reservation.getUuid())
                .address(hotel.getAddress())
                .build();
    }

}
