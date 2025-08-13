package com.beyond.HanSoom.chat.controller;

import com.beyond.HanSoom.chat.dto.ChatMessageDto;
import com.beyond.HanSoom.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/room/group/create")
    public ResponseEntity<?> createGroupRoom (@RequestParam String roomName){
        chatService.createGroupRoom(roomName);
        return ResponseEntity.ok().build();
    }


    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto chatMessageDto){
        System.out.println(chatMessageDto.getMessage());
        messageTemplate.convertAndSend("/topic/"+roomId, chatMessageDto);
    }

    @PostMapping("/room/{roomId}/read")
    public ResponseEntity<?> messageRead(@PathVariable Long roomId){
        chatService.messageRead(roomId);
        return ResponseEntity.ok().build();
    }

}
