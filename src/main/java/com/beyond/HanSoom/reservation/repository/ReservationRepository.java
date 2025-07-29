package com.beyond.HanSoom.reservation.repository;

import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByUser(User user);
    Reservation findByUser(User user);
}
