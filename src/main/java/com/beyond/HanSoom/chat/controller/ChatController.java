package com.beyond.HanSoom.chat.controller;

import com.beyond.HanSoom.chat.dto.ChatMessageResDto;
import com.beyond.HanSoom.chat.dto.ChatMyChatroomResDto;
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

//    @MessageMapping("/{roomId}")
//    public void sendMessage(ChatMessageDto message) {
//        // 서버 수신 시각 기준으로 timestamp 보정
//        System.out.println(message);
//       producer.publish(message);
//    }
    @PostMapping("/room/create/{reservationId}")
    public ResponseEntity<?> createChatRoom(@PathVariable Long reservationId){
        Long chatRoomId = chatService.createChatRoom(reservationId);
        return new ResponseEntity<>(new CommonSuccessDto(chatRoomId, HttpStatus.CREATED.value(), "채팅방 생성 완료"), HttpStatus.CREATED);
    }

    @PostMapping("/room/group/create")
    public ResponseEntity<?> createGroupRoom (@RequestParam String roomName){
        chatService.createGroupRoom(roomName);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<?> messageRead(@PathVariable Long roomId){
        chatService.messageRead(roomId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/room/private/create")
    public ResponseEntity<?> getOrCreatePrivateRoom(@RequestParam Long otherMemberId){
        Long roomId = chatService.getOrCreatePrivateRoom(otherMemberId);
        return new ResponseEntity<>(roomId, HttpStatus.OK);
    }

    //나의 채팅 방 조회
    @GetMapping("/room/list")
    public ResponseEntity<?> getMyChatRooms(){
        List<ChatMyChatroomResDto> dtos = chatService.getMyChatRoom();
        return new ResponseEntity<>(new CommonSuccessDto(dtos, HttpStatus.OK.value(), "채팅방 조회완료"), HttpStatus.OK);
    }

    //채팅 내역 조회
    @GetMapping("/history/{roomId}")
    public ResponseEntity<?> getChatHistory(@PathVariable Long roomId) {
        List<ChatMessageResDto> chatMessageDtos = chatService.getChatHistory(roomId);
        return new ResponseEntity<>(chatMessageDtos, HttpStatus.OK);
    }




}
