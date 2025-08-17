package com.beyond.HanSoom.chat.repository;

import com.beyond.HanSoom.chat.domain.ChatParticipant;
import com.beyond.HanSoom.chat.domain.ChatRoom;
import com.beyond.HanSoom.user.domain.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant,Long> {
    List<ChatParticipant> findByChatRoom(ChatRoom chatRoom);

//    @Query("select cp1.chatRoom from chatParticipant cp1 join chatParticipant cp2 on cp1.chatRoom.id = cp2.chatRoom.id where cp1.member.id = :myId and cp2.member.id = :otherMemberId and cp1.chatRoom.isGroupChat = 'N'")
//    Optional<ChatRoom> findChatRoomId(@Param("myId") Long id, @Param("otherMemberId") Long id1);
    Optional<ChatParticipant> findByChatRoomAndUser(ChatRoom chatRoom, User member);
}
