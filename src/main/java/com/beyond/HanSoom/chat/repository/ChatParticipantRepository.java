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

    @Query("SELECT cp1.chatRoom FROM ChatParticipant cp1 JOIN ChatParticipant cp2 ON cp1.chatRoom.id = cp2.chatRoom.id WHERE cp1.user.id = :guestId AND cp2.user.id = :hostId AND cp1.chatRoom.isGroupChat = 'N' " )
    Optional<ChatRoom> findExistingChatRoom(@Param("guestId") Long guestId, @Param("hostId") Long hostId);


    Optional<ChatParticipant> findByChatRoomAndUser(ChatRoom chatRoom, User member);

    List<ChatParticipant> findAllByUser(User user);

    List<ChatParticipant> findAllByChatRoom(ChatRoom chatRoom);

    Long countByChatRoom(ChatRoom chatRoom);

    int countByChatRoomAndIsOnline(ChatRoom cr, String y);
}
