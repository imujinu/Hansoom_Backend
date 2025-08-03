package com.beyond.HanSoom.reservation.dto.req;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.domain.State;
import com.beyond.HanSoom.room.domain.Room;
import com.beyond.HanSoom.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReservationReqDto {
    private Long hotelId;
    private Long roomId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private Long people ;
    private LocalDateTime reservationTime;
    private String request;

    public Reservation toEntity(Long price, User user, Hotel hotel, Room room){
        return Reservation.builder()
                .user(user)
                .hotel(hotel)
                .room(room)
                .checkInDate(this.checkIn)
                .checkOutDate(this.checkOut)
                .price(price)
                .reservationDate(this.reservationTime)
                .people(this.people)
                .request(this.request)
                .state(State.PENDING)
                .build();
    }
}
