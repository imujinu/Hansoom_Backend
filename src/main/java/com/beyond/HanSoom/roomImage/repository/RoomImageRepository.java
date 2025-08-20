package com.beyond.HanSoom.roomImage.repository;

import com.beyond.HanSoom.roomImage.domain.RoomImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {
    List<RoomImage> findByRoomId(Long roomId);
}
