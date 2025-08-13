package com.beyond.HanSoom.chat.service;

import com.beyond.HanSoom.chat.domain.ChatMessage;
import com.beyond.HanSoom.chat.domain.ChatReadStatus;
import com.beyond.HanSoom.chat.domain.ChatRoom;
import com.beyond.HanSoom.chat.domain.ChatParticipant;
import com.beyond.HanSoom.chat.dto.ChatMessageDto;
import com.beyond.HanSoom.chat.repository.ChatMessageRepository;
import com.beyond.HanSoom.chat.repository.ChatParticipantRepository;
import com.beyond.HanSoom.chat.repository.ChatReadStatusRepository;
import com.beyond.HanSoom.chat.repository.ChatRoomRepository;
import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {
     private final ChatMessageRepository chatMessageRepository;
     private final ChatReadStatusRepository chatReadStatusRepository;
     private final ChatRoomRepository chatRoomRepository;
     private final ChatParticipantRepository chatParticipantRepository;
     private final UserRepository userRepository;

     public void saveMessage(Long roomId, ChatMessageDto dto) {
          // 채팅방 조회
          ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("채팅방이 존재하지 않습니다."));

          // 보낸 사람 조회
          User user = userRepository.findByName(dto.getName()).orElseThrow(()->new EntityNotFoundException("존재하지 않는 유저 입니다."));

          // 메시지 저장
          ChatMessage chatMessage = ChatMessage.builder()
                  .chatRoom(chatRoom)
                  .content(dto.getMessage())
                  .user(user)
                  .build();
          chatMessageRepository.save(chatMessage);

          // 사용자 별로 읽음 여부 저장

          List<ChatParticipant> participantList = chatParticipantRepository.findByChatRoom(chatRoom);
          for(ChatParticipant p : participantList){
               ChatReadStatus readStatus = ChatReadStatus.builder()
                       .chatMessage(chatMessage)
                       .chatRoom(chatRoom)
                       .user(user)
                       .isRead(p.getUser().equals(user))
                       .build();

               chatReadStatusRepository.save(readStatus);
          }

     }

     public void createGroupRoom(String roomName) {


          User user = getUser();

          ChatRoom chatRoom = ChatRoom.builder()
                  .name(roomName)
                  .isGroupChat("Y")
                  .build();

          ChatParticipant chatParticipant = ChatParticipant.builder()
                  .chatRoom(chatRoom)
                  .user(user)
                  .build();

          chatRoom.getParticipantList().add(chatParticipant);

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
