package com.beyond.HanSoom.chat.controller;

import com.beyond.HanSoom.chat.domain.ChatAnnouncement;
import com.beyond.HanSoom.chat.dto.req.ChatAnnouncementReqDto;
import com.beyond.HanSoom.chat.dto.res.*;
import com.beyond.HanSoom.chat.service.ChatPublishService;
import com.beyond.HanSoom.chat.service.ChatService;
import com.beyond.HanSoom.common.dto.CommonSuccessDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final ChatPublishService producer;
    private final SimpMessageSendingOperations messageTemplate;

    // 예약완료 후 1:1 채팅방 생성
    @PostMapping("/room/create/{reservationId}")
    public ResponseEntity<?> createChatRoom(@PathVariable Long reservationId){
        Long chatRoomId = chatService.createChatRoom(reservationId);
        return new ResponseEntity<>(new CommonSuccessDto(chatRoomId, HttpStatus.CREATED.value(), "채팅방 생성 완료"), HttpStatus.CREATED);
    }
    //단체 채팅방 가입
    @PostMapping("/room/join/{reservationId}")
    public ResponseEntity<?> joinGroupChatRoom(@PathVariable Long reservationId){
        Long chatRoomId = chatService.joinGroupChatRoom(reservationId);
        return new ResponseEntity<>(new CommonSuccessDto(chatRoomId, HttpStatus.OK.value(), "단체 채팅방 입장 완료"), HttpStatus.OK);
    }

    //단체 채팅방 생성
    @PostMapping("/room/group/create")
    public ResponseEntity<?> createGroupRoom (){
        chatService.createGroupRoom();
        return ResponseEntity.ok().build();
    }
    //메시지 읽음 처리
    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<?> messageRead(@PathVariable Long roomId){
        chatService.messageRead(roomId);
        return ResponseEntity.ok().build();
    }

    //나의 채팅 방 조회
    @GetMapping("/room/private/list")
    public ResponseEntity<?> getMyChatPrivateRooms(){
        List<ChatMyChatroomResDto> dtos = chatService.getMyChatRoom();
        return new ResponseEntity<>(new CommonSuccessDto(dtos, HttpStatus.OK.value(), "채팅방 조회완료"), HttpStatus.OK);
    }

    //전체 채팅 방 조회
    @GetMapping("/room/group/list")
    public ResponseEntity<?> getMyChatGroupRooms(){
        List<ChatMyChatroomResDto> dtos = chatService.getMyGroupChatRoom();
        return new ResponseEntity<>(new CommonSuccessDto(dtos, HttpStatus.OK.value(), "채팅방 조회완료"), HttpStatus.OK);
    }
    //채팅 내역 조회
    @GetMapping("/history/{roomId}")
    public ResponseEntity<?> getChatHistory(@PathVariable Long roomId) {
        List<ChatMessageResDto> chatMessageDtos = chatService.getChatHistory(roomId);
        return new ResponseEntity<>(chatMessageDtos, HttpStatus.OK);
    }

    //호스트 호텔 내역 조회
    @GetMapping("/host/hotel")
    public ResponseEntity<?> getHostHotel() {
        List<ChatHotelResDto> dtos = chatService.getHostHotelList();
        return new ResponseEntity<>(new CommonSuccessDto(dtos, HttpStatus.OK.value(), "내 호텔 조회 완료"), HttpStatus.OK);
    }

    //호스트 채팅 내역 조회
    @GetMapping("/host/all")
    public ResponseEntity<?> getHostPrivateChatRoom() {
        List<ChatHostChatRoomResDto> dto = chatService.getHostChatRoom();
        return new ResponseEntity<>(new CommonSuccessDto(dto, HttpStatus.OK.value(), "1:1 채팅 조회 완료"), HttpStatus.OK);
    }

    //호스트 단체 채팅 생성
    @PostMapping("/host/{hotelId}")
    public ResponseEntity<?> createHostGroupChat(@PathVariable Long hotelId) {
        ChatHostChatRoomResDto dto = chatService.createHostGroupChat(hotelId);
        return new ResponseEntity<>(new CommonSuccessDto(dto, HttpStatus.OK.value(), "단체 채팅 생성 완료"), HttpStatus.OK);
    }

    //단체 채팅 멤버 조회
    @GetMapping("/room/{roomId}/group-user-list")
    public ResponseEntity<?> getGroupChatUserList(@PathVariable Long roomId){
        List<ChatGroupUserListResDto> dtos = chatService.getGroupChatUserList(roomId);
        return new ResponseEntity<>(new CommonSuccessDto(dtos, HttpStatus.OK.value(), "단체 채팅 멤버 조회 완료"), HttpStatus.OK);
    }

    // 공지사항 추가
    @PostMapping("/host/announcement")
    public ResponseEntity<?> addChatAnnouncement(@RequestBody ChatAnnouncementReqDto dto){
        List<ChatAnnouncement> dtos = chatService.addChatAnnouncement(dto);

        return new ResponseEntity<>(new CommonSuccessDto(dtos, HttpStatus.OK.value(), "공지사항 추가 완료"), HttpStatus.OK);
    }
}
