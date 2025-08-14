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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ChatService {
     private final ChatMessageRepository chatMessageRepository;
     private final ChatReadStatusRepository chatReadStatusRepository;
     private final ChatRoomRepository chatRoomRepository;
     private final ChatParticipantRepository chatParticipantRepository;
     private final UserRepository userRepository;
     private final StringRedisTemplate stringRedisTemplate;

    public ChatService(ChatMessageRepository chatMessageRepository, ChatReadStatusRepository chatReadStatusRepository, ChatRoomRepository chatRoomRepository, ChatParticipantRepository chatParticipantRepository, UserRepository userRepository, @Qualifier("redisStream") StringRedisTemplate stringRedisTemplate) {
        this.chatMessageRepository = chatMessageRepository;
        this.chatReadStatusRepository = chatReadStatusRepository;
        this.chatRoomRepository = chatRoomRepository;
        this.chatParticipantRepository = chatParticipantRepository;
        this.userRepository = userRepository;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public void publishMessage(ChatMessageDto chatMessageDto) {
          String streamKey = "chat:room:" + chatMessageDto.getRoomId();
         ObjectMapper objectMapper = new ObjectMapper();
        String json = null;
        try {
            json = objectMapper.writeValueAsString(chatMessageDto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        ObjectRecord<String, String> record = StreamRecords.newRecord()
                  .ofObject(json)
                  .withStreamKey(streamKey);
          try{
          RecordId recordId = stringRedisTemplate.opsForStream().add(record);
          } catch (Exception e) {
               throw new RuntimeException("메시지 발행에 실패했습니다.");
          }

     }



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


     public void addParticipantToGroupChat(Long roomId) {
          // 채팅방 조회
          ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException(""));
          // 멤버 조회
          User user = userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(()->new EntityNotFoundException(""));
          // 이미 참여자인지 검증
          Optional<ChatParticipant> participant = chatParticipantRepository.findByChatRoomAndUser(chatRoom,user);

          // ChatParticipant 객체 생성 후 저장

          if(!participant.isPresent()){
               ChatParticipant chatParticipant = ChatParticipant.builder()
                       .chatRoom(chatRoom)
                       .user(user)
                       .build();

               chatParticipantRepository.save(chatParticipant);

          }
     }

     public void messageRead(Long roomId) {
          ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("채팅방이 존재하지 않습니다."));
          User user = getUser();
          List<ChatReadStatus> readStatuses = chatReadStatusRepository.findByChatRoomAndUser(chatRoom,user);

          for(ChatReadStatus r : readStatuses){
               r.updateIsRead(true);
          }


     }

     public void addParticipantChatRoom(ChatRoom chatRoom, User user){
          ChatParticipant chatParticipant = ChatParticipant.builder()
                  .chatRoom(chatRoom)
                  .user(user)
                  .build();

          chatParticipantRepository.save(chatParticipant);

     }
     public Long getOrCreatePrivateRoom(Long otherMemberId) {
          User user = getUser();
          User other = userRepository.findById(otherMemberId).orElseThrow(()->new EntityNotFoundException("존재하지 않는 유저입니다."));

//          Optional<ChatRoom> chatRoom = chatParticipantRepository.findChatRoomId(user.getId(), other.getId());
         Optional<ChatRoom> chatRoom = Optional.ofNullable(ChatRoom.builder().build());
          if(chatRoom.isPresent()){
               return chatRoom.get().getId();
          }

               ChatRoom newRoom = ChatRoom.builder()
                       .isGroupChat("N")
                       .name(user.getName() + "-" + other.getName())
                       .build();
               addParticipantChatRoom(newRoom,user);
               addParticipantChatRoom(newRoom,other);
          return newRoom.getId();
     }

     private User getUser() {
          return userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 유저입니다."));
     }
}
