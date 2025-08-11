package com.beyond.HanSoom.reservation.dto.res;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.room.domain.Room;
import com.beyond.HanSoom.user.domain.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationCacheResDto {
    private String uuid;
    private Long id;
    private Long userId;
    private String userName;
    private Long hotelId;
    private String hotelName;
    private Long roomId;
    private String roomType;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Long price;
    private LocalDateTime reservationDate;
    private long people;
    private String request;
    private String state;

    // Entity -> DTO 변환 메서드
    public static ReservationCacheResDto fromEntity(Reservation reservation) {
        return ReservationCacheResDto.builder()
                .uuid(reservation.getUuid())
                .id(reservation.getId())
                .userId(reservation.getUser().getId())
                .userName(reservation.getUser().getName())
                .hotelId(reservation.getHotel().getId())
                .hotelName(reservation.getHotel().getHotelName())
                .roomId(reservation.getRoom().getId())
                .roomType(reservation.getRoom().getType())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .price(reservation.getPrice())
                .reservationDate(reservation.getReservationDate())
                .people(reservation.getPeople())
                .request(reservation.getRequest())
                .state(reservation.getState().name())
                .build();
    }
}
