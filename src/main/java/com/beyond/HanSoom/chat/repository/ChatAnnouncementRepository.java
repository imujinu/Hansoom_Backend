package com.beyond.HanSoom.chat.repository;

import com.beyond.HanSoom.chat.domain.ChatAnnouncement;
import com.beyond.HanSoom.chat.domain.ChatRoom;
import com.beyond.HanSoom.chat.dto.res.ChatAnnouncementResDto;
import com.beyond.HanSoom.hotel.domain.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatAnnouncementRepository extends JpaRepository<ChatAnnouncement, Long> {

    List<ChatAnnouncement> findAllByHotel(Hotel hotel);
}
