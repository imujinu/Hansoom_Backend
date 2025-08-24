package com.beyond.HanSoom.chat.service;

import com.beyond.HanSoom.chat.domain.ChatMessage;
import com.beyond.HanSoom.chat.domain.ChatReadStatus;
import com.beyond.HanSoom.chat.domain.ChatRoom;
import com.beyond.HanSoom.chat.domain.ChatParticipant;
import com.beyond.HanSoom.chat.dto.*;
import com.beyond.HanSoom.chat.repository.ChatMessageRepository;
import com.beyond.HanSoom.chat.repository.ChatParticipantRepository;
import com.beyond.HanSoom.chat.repository.ChatReadStatusRepository;
import com.beyond.HanSoom.chat.repository.ChatRoomRepository;
import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.repository.HotelRepository;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.repository.ReservationRepository;
import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
     private final HotelRepository hotelRepository;


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
          chatRoom.addMessage(chatMessage);
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
                .isGroupChat("N")
                .build();
        addParticipantChatRoom(newRoom, host);
        addParticipantChatRoom(newRoom, guest);
        chatRoomRepository.save(newRoom);
        return newRoom.getId();
    }


     public void createGroupRoom() {
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
            Long unReadCount = getUnReadCount(chatRoom, user);
            Long participantCount = chatParticipantRepository.countByChatRoom(chatRoom);
            return ChatMyChatroomResDto.builder()
                    .roomId(chatRoom.getId())
                    .hotelName(hotel.getHotelName())
                    .isGroupChat(chatRoom.getIsGroupChat())
                    .unReadCount(unReadCount)
                    .participants(participantCount)
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
                    .roomId(chatRoom.getId())
                    .timestamp(String.valueOf(c.getCreatedTime()))
                    .content(c.getContent())
                    .senderName(c.getUser().getName())
                    .senderEmail(c.getUser().getEmail())
                    .build();
            chatMessageDtos.add(chatMessageDto);
        }
        return chatMessageDtos;
    }




    public List<ChatMyChatroomResDto> getMyGroupChatRoom() {
         User user = getUser();
         List<ChatMyChatroomResDto> chatRooms = new ArrayList<>();
         List<Reservation> reservations = reservationRepository.findAllByUser(user);
        LocalDate now = LocalDate.now();
         for(Reservation r : reservations){
             // 예약 날짜가 체크인-1 < now < 체크아웃+1 사이에 있을 때만 보여주기
             if(!r.getCheckInDate().minusDays(1).isAfter(now) && !r.getCheckOutDate().isBefore(now)){

             //
             Hotel hotel = r.getHotel();
             ChatRoom chatRoom = chatRoomRepository.findByHotelAndIsGroupChat(hotel,"Y");
             if(chatRoom!=null){

                 Long unReadCount = getUnReadCount(chatRoom, user);
                 chatRooms.add(new ChatMyChatroomResDto().fromEntity(chatRoom,unReadCount));
             }
             }

         }

         return chatRooms;
    }

    private Long getUnReadCount(ChatRoom chatRoom, User user) {
        Long unReadCount = chatReadStatusRepository.countByChatRoomAndUserAndIsReadFalse(chatRoom, user);
        return unReadCount;
    }

    public List<ChatHotelResDto> getHostHotelList() {
        User host =getUser();
        List<Hotel> hotels = hotelRepository.findAllByUser(host);
        List<ChatHotelResDto> resDtoList = new ArrayList<>();
        for(Hotel h : hotels){
            ChatHotelResDto dto = ChatHotelResDto.builder()
                    .hotelId(h.getId())
                    .name(h.getHotelName())
                    .build();
            resDtoList.add(dto);
        }
        return resDtoList;
    }

    private User getUser() {
        return userRepository.findByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 유저입니다."));
    }

    //호스트 1:1 채팅 방 조회
    public List<ChatHostPrivateChatRoomResDto> getHostPrivateChatRoom(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(()->new EntityNotFoundException("존재하지 않는 호텔 입니다."));
        User host = getUser();
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByHotelAndIsGroupChat(hotel, "N");
        if(chatRooms.isEmpty()){
            return null;
        }
        List<ChatHostPrivateChatRoomResDto> dtos = new ArrayList<>();

        for(ChatRoom cr : chatRooms){
            ChatParticipant participant = cr.getParticipantList().stream().filter(cp -> !cp.getUser().equals(host)).findFirst().orElseThrow(()->new IllegalArgumentException("호스트 외의 유저가 존재하지 않습니다."));
            String guestName = participant.getUser().getName();
            ChatMessage chatMessage = cr.getChatMessageList().stream().filter(cm -> !cm.getUser().equals(host)).reduce((first,second)->second).orElse(null);
            Long unreadCount = getUnReadCount(cr,host);

            dtos.add(ChatHostPrivateChatRoomResDto.builder()
                    .id(cr.getId())
                    .guestName(guestName)
                    .lastMessage(chatMessage.getContent())
                    .timestamp(chatMessage.getCreatedTime())
                    .unreadCount(unreadCount)
                    .build());

        }
        return dtos;
    }

    //호스트 단체 채팅 방 조회
    public ChatHostGroupChatRoomResDto getHostGroupChatRoom(Long hotelId) {
         Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(()->new EntityNotFoundException("존재하지 않는 호텔 입니다."));
         ChatRoom chatRoom = chatRoomRepository.findByHotelAndIsGroupChat(hotel, "Y");
         if(chatRoom==null){
             return null;
         }
         ChatMessage message = null;
        if (!chatRoom.getChatMessageList().isEmpty()) {
            message = chatRoom.getChatMessageList()
                    .get(chatRoom.getChatMessageList().size() - 1);

        }
        return new ChatHostGroupChatRoomResDto().fromEntity(chatRoom, message);
    }

    public ChatHostGroupChatRoomResDto createHostGroupChat(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(()->new EntityNotFoundException("존재하지 않는 호텔 입니다."));
        User host = getUser();

        ChatRoom chatRoom = ChatRoom.builder()
                .isGroupChat("Y")
                .hotel(hotel)
                .build();

        chatRoomRepository.save(chatRoom);
        addParticipantToGroupChat(chatRoom.getId());

        ChatMessageResDto dto = ChatMessageResDto.builder()
                .roomId(chatRoom.getId())
                .content("채팅방 생성이 완료되었습니다.")
                .timestamp(String.valueOf(LocalDateTime.now()))
                .senderEmail(host.getEmail())
                .senderName(host.getName())
                .build();

        saveMessage(dto);
        ChatMessage chatMessage = chatRoom.getChatMessageList().get(chatRoom.getChatMessageList().size()-1);
        return new ChatHostGroupChatRoomResDto().fromEntity(chatRoom,chatMessage);

    }

    public void updateOnlineState(String email, String roomId, String state) {
         ChatRoom chatRoom = chatRoomRepository.findById(Long.valueOf(roomId)).orElseThrow(()->new EntityNotFoundException("채팅방이 존재하지 않습니다."));
         User user = userRepository.findByEmail(email).orElseThrow(()->new EntityNotFoundException("존재하지 않는 유저입니다."));
         ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomAndUser(chatRoom,user).orElseThrow(()->new EntityNotFoundException("존재하지 않는 채팅 참여자 입니다."));
         chatParticipant.updateOnlineState(state);
    }

    public List<ChatGroupUserListResDto> getGroupChatUserList(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("채팅방이 존재하지 않습니다."));
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByChatRoom(chatRoom);

        return chatParticipants.stream().map(cp -> new ChatGroupUserListResDto().fromEntity(cp)).collect(Collectors.toList());
    }

    public Long joinGroupChatRoom(Long reservationId) {
         Reservation reservation = reservationRepository.findById(reservationId).orElseThrow(()->new EntityNotFoundException("예약 내역이 존재하지 않습니다."));
         Hotel hotel = reservation.getHotel();
         ChatRoom chatRoom = chatRoomRepository.findByHotelAndIsGroupChat(hotel, "Y");
         User user = getUser();
         addParticipantChatRoom(chatRoom, user);
         return chatRoom.getId();
    }


}
