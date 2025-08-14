package com.beyond.HanSoom.chat.controller;

import com.beyond.HanSoom.chat.dto.ChatMessageDto;
import com.beyond.HanSoom.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;
    private final SimpMessageSendingOperations messageTemplate;

    @PostMapping("/chat/send")
    public void sendMessage(@RequestBody ChatMessageDto chatMessageDto) {
        chatService.publishMessage(chatMessageDto);
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




}
