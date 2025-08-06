package com.beyond.HanSoom.hotel.repository;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.domain.HotelState;
import com.beyond.HanSoom.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    Page<Hotel> findAll(Pageable pageable);
    Page<Hotel> findAll(Specification<Hotel> spec, Pageable pageable);

    Optional<Hotel> findByUserAndState(User user, HotelState hotelState);

    Optional<Hotel> findByIdAndState(Long id, HotelState hotelState);
}
