package com.beyond.HanSoom.chat.service;

import com.beyond.HanSoom.chat.domain.ChatMessage;
import com.beyond.HanSoom.chat.domain.ChatReadStatus;
import com.beyond.HanSoom.chat.domain.ChatRoom;
import com.beyond.HanSoom.chat.domain.ChatParticipant;
import com.beyond.HanSoom.chat.dto.ChatMessageResDto;
import com.beyond.HanSoom.chat.dto.ChatMyChatroomResDto;
import com.beyond.HanSoom.chat.repository.ChatMessageRepository;
import com.beyond.HanSoom.chat.repository.ChatParticipantRepository;
import com.beyond.HanSoom.chat.repository.ChatReadStatusRepository;
import com.beyond.HanSoom.chat.repository.ChatRoomRepository;
import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.repository.ReservationRepository;
import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {
     private final ChatMessageRepository chatMessageRepository;
     private final ChatReadStatusRepository chatReadStatusRepository;
     private final ChatRoomRepository chatRoomRepository;
     private final ChatParticipantRepository chatParticipantRepository;
     private final UserRepository userRepository;
     private final ReservationRepository reservationRepository;


     public void saveMessage(ChatMessageResDto dto) {
          // 채팅방 조회
          ChatRoom chatRoom = chatRoomRepository.findById(dto.getRoomId()).orElseThrow(()->new EntityNotFoundException("채팅방이 존재하지 않습니다."));

          // 보낸 사람 조회
          User user = userRepository.findByEmail(dto.getSenderEmail()).orElseThrow(()->new EntityNotFoundException("존재하지 않는 유저 입니다."));

          // 메시지 저장
          ChatMessage chatMessage = ChatMessage.builder()
                  .chatRoom(chatRoom)
                  .content(dto.getContent())
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
    public Long createChatRoom(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(()->new EntityNotFoundException("예약 내역이 존재하지 않습니다."));
        User host = reservation.getHotel().getUser();
        User guest = getUser();
        Optional<ChatRoom> chatRoom = chatParticipantRepository.findExistingChatRoom(guest.getId(), host.getId());

        if(chatRoom.isPresent()){
            return chatRoom.get().getId();
        }
        ChatRoom newRoom = ChatRoom.builder()
                .hotel(reservation.getHotel())
                .build();
        addParticipantChatRoom(newRoom, host);
        addParticipantChatRoom(newRoom, guest);

        return newRoom.getId();
    }


     public void createGroupRoom(String roomName) {
          User user = getUser();

          ChatRoom chatRoom = ChatRoom.builder()
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
                       .build();
               addParticipantChatRoom(newRoom,user);
               addParticipantChatRoom(newRoom,other);
          return newRoom.getId();
     }


    public List<ChatMyChatroomResDto> getMyChatRoom() {
        //1. 유저 조회 -> 2. chatParticipant 조회 -> 3. 채팅방 조회 -> 4. dto 조립해서 리턴
        User user = getUser();
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByUser(user);
        List<ChatMyChatroomResDto> dtos = chatParticipants.stream().map(cp->{
            ChatRoom chatRoom = cp.getChatRoom();
            Hotel hotel = chatRoom.getHotel();
            Long unReadCount = chatReadStatusRepository.findByChatRoomAndUserAndIsReadFalse(user,chatRoom);
            return ChatMyChatroomResDto.builder()
                    .roomId(chatRoom.getId())
                    .hotelName(hotel.getHotelName())
                    .isGroupChat(chatRoom.getIsGroupChat())
                    .ueReadCount(unReadCount)
                    .build();

        }).collect(Collectors.toList());


        return dtos;
    }

    public List<ChatMessageResDto> getChatHistory(Long roomId) {
        // 내가 해당 채팅방의 참여자가 아닐 경우 에러
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException(""));
        User user = getUser();
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findByChatRoom(chatRoom);
        boolean check = false;
        for(ChatParticipant c : chatParticipants){
            if(c.getUser().equals(user)){
                check=true;
            }
        }
        if(!check)throw new IllegalStateException("본인이 속하지 않은 채팅방입니다.");
        // 특정 room 에대한 message
        List<ChatMessage> chatMessages = chatMessageRepository.findByChatRoomOrderByCreatedTimeAsc(chatRoom);
        List<ChatMessageResDto> chatMessageDtos = new ArrayList<>();
        for(ChatMessage c : chatMessages){
            ChatMessageResDto chatMessageDto = ChatMessageResDto.builder()
                    .content(c.getContent())
                    .senderEmail(user.getEmail())
                    .build();
            chatMessageDtos.add(chatMessageDto);
        }
        return chatMessageDtos;
    }

    private User getUser() {
        return userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 유저입니다."));
    }


}
