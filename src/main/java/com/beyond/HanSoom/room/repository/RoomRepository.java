package com.beyond.HanSoom.room.repository;

import com.beyond.HanSoom.room.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
