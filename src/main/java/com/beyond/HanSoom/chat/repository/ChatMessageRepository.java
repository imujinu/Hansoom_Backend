package com.beyond.HanSoom.chat.repository;

import com.beyond.HanSoom.chat.domain.ChatMessage;
import com.beyond.HanSoom.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {
    List<ChatMessage> findByChatRoomOrderByCreatedTimeAsc(ChatRoom chatRoom);


    Optional<ChatMessage> findTopByChatRoomIdOrderByCreatedTimeDesc(Long id);
}
