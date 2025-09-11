package com.beyond.HanSoom.chat.service;

import com.beyond.HanSoom.chat.domain.*;
import com.beyond.HanSoom.chat.dto.req.ChatActivateReqDto;
import com.beyond.HanSoom.chat.dto.req.ChatAnnouncementReqDto;
import com.beyond.HanSoom.chat.dto.req.ChatCreateReqDto;
import com.beyond.HanSoom.chat.dto.res.ChatAnnouncementResDto;
import com.beyond.HanSoom.chat.dto.res.*;
import com.beyond.HanSoom.chat.repository.*;
import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.repository.HotelRepository;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.repository.ReservationRepository;
import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.domain.UserRole;
import com.beyond.HanSoom.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
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
     private final ChatAnnouncementRepository chatAnnouncementRepository;


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
    public Long createChatRoom(ChatCreateReqDto dto) {
        Reservation reservation = reservationRepository.findById(dto.getReservationId()).orElseThrow(()->new EntityNotFoundException("예약 내역이 존재하지 않습니다."));
        User host = reservation.getHotel().getUser();
        User guest = getUser();
        Optional<ChatRoom> chatRoom = chatParticipantRepository.findExistingChatRoom(guest.getId(), host.getId());

        if(chatRoom.isPresent()){
            return chatRoom.get().getId();
        }

        ChatRoom newRoom = ChatRoom.builder()
                .hotel(reservation.getHotel())
                .reservation(reservation)
                .isGroupChat("N")
                .build();

        addParticipantChatRoom(newRoom, host);
        addParticipantChatRoom(newRoom, guest);
        chatRoomRepository.save(newRoom);
        ChatParticipant hostParticipant = chatParticipantRepository.findByChatRoomAndUser(newRoom, host).orElseThrow(()-> new EntityNotFoundException("채팅 참여자가 존재하지 않습니다."));
        ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomAndUser(newRoom,guest).orElseThrow(()-> new EntityNotFoundException("채팅 참여자가 존재하지 않습니다."));
        chatParticipant.setKey(dto.getAesKey(), dto.getIv());
        hostParticipant.setKey(dto.getAesKey(), dto.getIv());
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


     public void addParticipantToGroupChat(Long roomId, User user) {
          // 채팅방 조회
          ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException(""));
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
            List<ChatMyChatroomResDto> dtos = chatParticipantRepository.findAllByUser(user)
                    .stream()
                    .map(cp -> {
                        ChatRoom chatRoom = cp.getChatRoom();
                        Long unReadCount = getUnReadCount(chatRoom,user);
                        Optional<ChatMessage> message = Optional.ofNullable(chatRoom.getChatMessageList()).filter(list -> !list.isEmpty()).map(list -> list.get(list.size()-1));
                        LocalDateTime lastMessageTime = message.map(ChatMessage::getCreatedTime).orElse(null);
                        String lastMessage = message.map(ChatMessage::getContent).orElse("");

                        String isOnline = chatRoom.getParticipantList().stream()
                                .filter(p-> p.getUser().equals(chatRoom.getHotel().getUser()))
                                .map(ChatParticipant::getIsOnline)
                                .findFirst()
                                .orElse(null);

                        return new ChatMyChatroomResDto().fromEntity(chatRoom, unReadCount,lastMessage, lastMessageTime,isOnline);
                    })
                    .toList();
            System.out.println("호스트==========" + dtos);
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
        ChatParticipant me = chatParticipantRepository.findByChatRoomAndUser(chatRoom,user).orElseThrow(()->new EntityNotFoundException("존재하지 않는 유저입니다."));
        User hostUser = chatRoom.getHotel().getUser();
        ChatParticipant host = chatParticipantRepository.findByChatRoomAndUser(chatRoom,hostUser).orElseThrow(()->new EntityNotFoundException("채팅 유저가 존재하지 않습니다."));
        for(ChatMessage c : chatMessages){
            Map<String, String> keysMap = new HashMap<>();


            ChatMessageResDto chatMessageDto = ChatMessageResDto.builder()
                    .roomId(chatRoom.getId())
                    .content(c.getContent())
                    .timestamp(String.valueOf(c.getCreatedTime()))
                    .senderEmail(c.getUser().getEmail())
                    .senderName(c.getUser().getName())
                    .profileImage(c.getUser().getProfileImage())
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
//                 chatRooms.add(new ChatMyChatroomResDto().fromEntity(chatRoom,unReadCount));
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
    public List<ChatHostChatRoomResDto> getHostChatRoom() {
         User host =getUser();
        List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByUser(host);

        if(chatParticipants.isEmpty()){
            throw new IllegalArgumentException("채팅방이 존재하지 않습니다.");
        }

        List<ChatHostChatRoomResDto> dtos = new ArrayList<>();

        for(ChatParticipant cp : chatParticipants){
            ChatRoom cr = cp.getChatRoom();
            ChatMessage chatMessage = chatMessageRepository
                    .findTopByChatRoomIdOrderByCreatedTimeDesc(cr.getId()).orElse(null);
            int isOnline = chatParticipantRepository.countByChatRoomAndIsOnline(cr,"Y");
            long unReadCount = getUnReadCount(cr,host);
            String guestName = null;

            if(cr.getIsGroupChat().equals("N")){
                User guest = cr.getParticipantList().stream().map(ChatParticipant::getUser).filter(user -> !user.equals(host)).findFirst().orElse(null);
                dtos.add(new ChatHostChatRoomResDto().fromEntity(cr, chatMessage, guest.getName(),unReadCount ));
            }else{

           dtos.add(new ChatHostChatRoomResDto().fromEntity(cr, chatMessage, isOnline,unReadCount ));
            }
        }
        return dtos;
    }

    //호스트 단체 채팅 방 조회
//    public ChatHostGroupChatRoomResDto getHostGroupChatRoom(Long hotelId) {
//         Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(()->new EntityNotFoundException("존재하지 않는 호텔 입니다."));
//         ChatRoom chatRoom = chatRoomRepository.findByHotelAndIsGroupChat(hotel, "Y");
//         if(chatRoom==null){
//             return null;
//         }
//         ChatMessage message = null;
//        if (!chatRoom.getChatMessageList().isEmpty()) {
//            message = chatRoom.getChatMessageList()
//                    .get(chatRoom.getChatMessageList().size() - 1);
//
//        }
//        List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByChatRoom(chatRoom);
//        int isOnline = 0;
//        for(ChatParticipant cp : chatParticipants){
//            if(cp.getIsOnline().equals("Y")){
//                isOnline++;
//            }
//        }
//        return new ChatHostGroupChatRoomResDto().fromEntity(chatRoom, message, isOnline);
//    }

    public void createHostGroupChat() {
        User user = getUser();
        if(!user.getUserRole().equals(UserRole.HOST)){
            return;
        }
        Hotel hotel = hotelRepository.findByUser(user);
        ChatRoom groupChat = chatRoomRepository.findByHotelAndIsGroupChat(hotel,"Y");

        if(groupChat!=null){
            return;
        }

        ChatRoom chatRoom = ChatRoom.builder()
                .isGroupChat("Y")
                .hotel(hotel)
                .build();

        chatRoomRepository.save(chatRoom);
        addParticipantChatRoom(chatRoom,user);

        return;

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
         Optional<ChatParticipant> chatParticipant = chatParticipantRepository.findByChatRoomAndUser(chatRoom,user);
         if(chatParticipant.isPresent()){
             return chatRoom.getId();
         }
         addParticipantChatRoom(chatRoom, user);
         return chatRoom.getId();
    }


    public ChatAnnouncementResDto addChatAnnouncement(ChatAnnouncementReqDto dto) {
         Hotel hotel = hotelRepository.findById(dto.getHotelId()).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 호텔입니다."));
         ChatAnnouncement chatAnnouncement = new ChatAnnouncementReqDto().toEntity(dto,hotel);
         ChatAnnouncement ch = chatAnnouncementRepository.save(chatAnnouncement);
         return new ChatAnnouncementResDto().fromEntity(ch, hotel);
    }

    public List<ChatAnnouncementResDto> getChatAnnouncements() {
         Hotel hotel = hotelRepository.findByUser(getUser());
         List<ChatAnnouncement> chatAnnouncements = chatAnnouncementRepository.findAllByHotel(hotel);
         return chatAnnouncements.stream().map(ca->new ChatAnnouncementResDto().fromEntity(ca,hotel)).collect(Collectors.toList());
    }

    public void deleteChatAnnouncements(Long id) {
         chatAnnouncementRepository.deleteById(id);
    }

    public void activateChatAnnouncements(ChatActivateReqDto dto) {
         for(Long id : dto.getIds()){
             ChatAnnouncement chatAnnouncement = chatAnnouncementRepository.findById(id).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 공지사항 입니다."));
             chatAnnouncement.changeActive(dto.getIsActive());
         }
    }

    public List<ChatAnnouncementResDto> getChatRoomAnnouncements(Long roomId) {
         ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()-> new EntityNotFoundException("존재하지 않는 채팅방입니다."));
         Hotel hotel = chatRoom.getHotel();
         List<ChatAnnouncement> chatAnnouncementList = chatAnnouncementRepository.findAllByHotel(hotel);
        return chatAnnouncementList.stream().map(ca->new ChatAnnouncementResDto().fromEntity(ca,hotel)).collect(Collectors.toList());
    }

    public List<Long> getAllChatRooms() {
         User user = getUser();
         List<ChatParticipant> chatParticipants = chatParticipantRepository.findAllByUser(user);

         if(chatParticipants.isEmpty()){
             return null;
         }

         return chatParticipants.stream().map(ch -> ch.getChatRoom().getId()).collect(Collectors.toList());
    }

    public Long getRemaining(Long roomId) {
         User user = getUser();
         ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("존재하지 않는 채팅방입니다."));
         ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomAndUser(chatRoom,user).orElseThrow(()->new EntityNotFoundException("존재하지 않는 채팅유저입니다."));
        System.out.println(chatParticipant.getRemaining());
         return chatParticipant.getRemaining();
    }

    public void deleteChatAnnouncements(List<Long> announcements){
         for(Long l : announcements){
             chatAnnouncementRepository.deleteById(l);
         }
    }

    public ChatKeyResDto getKeys(Long roomId) {
         ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("채팅방이 존재하지 않습니다."));
         List<ChatParticipant> list = chatRoom.getParticipantList();
         User user = getUser();

         ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomAndUser(chatRoom,user).orElseThrow(()-> new EntityNotFoundException("채팅 참여자가 존재하지 않습니다."));
         return new ChatKeyResDto().fromEntity(chatParticipant);
    }

    public void exitRoom(Long roomId) {
         User user = getUser();
         ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(()->new EntityNotFoundException("채팅방이 존재하지 않습니다."));
         ChatParticipant chatParticipant = chatParticipantRepository.findByChatRoomAndUser(chatRoom,user).orElseThrow(()->new EntityNotFoundException("존재하지 않는 채팅 유저입니다."));
         chatParticipantRepository.delete(chatParticipant);
         if(chatRoom.getIsGroupChat()=="N"){
             chatRoomRepository.delete(chatRoom);
         }
    }
}
