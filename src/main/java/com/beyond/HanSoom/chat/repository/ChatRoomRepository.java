package com.beyond.HanSoom.chat.repository;

import com.beyond.HanSoom.chat.domain.ChatMessage;
import com.beyond.HanSoom.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom,Long> {
}
