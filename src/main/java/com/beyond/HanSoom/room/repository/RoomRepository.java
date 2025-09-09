package com.beyond.HanSoom.room.repository;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.room.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    Optional<Room> findByIdAndHotel(Long roomId, Hotel hotel);

    List<Room> findByHotelId(Long id);
}
