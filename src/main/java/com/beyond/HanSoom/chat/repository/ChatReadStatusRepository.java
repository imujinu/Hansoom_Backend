package com.beyond.HanSoom.chat.repository;

import com.beyond.HanSoom.chat.domain.ChatMessage;
import com.beyond.HanSoom.chat.domain.ChatReadStatus;
import com.beyond.HanSoom.chat.domain.ChatRoom;
import com.beyond.HanSoom.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatReadStatusRepository extends JpaRepository<ChatReadStatus,Long> {
    List<ChatReadStatus> findByChatRoomAndUser(ChatRoom chatRoom, User user);


    Long countByChatRoomAndUserAndIsReadFalse(ChatRoom chatRoom, User user);
}
