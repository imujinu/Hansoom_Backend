package com.beyond.HanSoom.chat.service;

import com.beyond.HanSoom.chat.domain.ChatReadStatus;
import com.beyond.HanSoom.chat.domain.ChatRoom;
import com.beyond.HanSoom.chat.domain.ChatUser;
import com.beyond.HanSoom.chat.repository.ChatMessageRepository;
import com.beyond.HanSoom.chat.repository.ChatReadStatusRepository;
import com.beyond.HanSoom.chat.repository.ChatRoomRepository;
import com.beyond.HanSoom.chat.repository.ChatUserRepository;
import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {
     private final ChatMessageRepository chatMessageRepository;
     private final ChatReadStatusRepository chatReadStatusRepository;
     private final ChatRoomRepository chatRoomRepository;
     private final ChatUserRepository chatUserRepository;
     private final UserRepository userRepository;

     public void createGroupRoom(String roomName) {


          User user = getUser();

          ChatRoom chatRoom = ChatRoom.builder()
                  .name(roomName)
                  .isGroupChat("Y")
                  .build();

          ChatUser chatUser = ChatUser.builder()
                  .chatRoom(chatRoom)
                  .user(user)
                  .build();

          chatRoom.getParticipantList().add(chatUser);

          chatRoomRepository.save(chatRoom);
     }

     private User getUser() {
          return userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 유저입니다."));
     }


     public void messageRead(Long roomId) {
          ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("채팅방이 존재하지 않습니다."));
          User user = getUser();
          List<ChatReadStatus> readStatuses = chatReadStatusRepository.findByChatRoomAndUser(chatRoom,user);

          for(ChatReadStatus r : readStatuses){
               r.updateIsRead(true);
          }


     }
}
