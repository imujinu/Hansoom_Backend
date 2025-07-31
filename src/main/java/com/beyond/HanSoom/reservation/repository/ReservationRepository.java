package com.beyond.HanSoom.reservation.repository;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.room.domain.Room;
import com.beyond.HanSoom.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {


    List<Reservation> findAllByUser(User user);

    Reservation findByUser(User user);

    @Query("select r from reservation r where r.hotel=:hotel and r.room=:room and r.user=:user and r.checkIn < :checkOut and r.checkOut > :checkIn  ")
    int checkRoom(@Param("user")User user, @Param("room")Room room, @Param("hotel")Hotel hotel, @Param("checkIn")LocalDate checkIn, @Param("checkOut")LocalDate checkOut);
}
