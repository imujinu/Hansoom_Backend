package com.beyond.HanSoom.chat.controller;

import com.beyond.HanSoom.chat.dto.ChatMessageDto;
import com.beyond.HanSoom.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class StompController {
    private final ChatService chatService;
    private final SimpMessageSendingOperations messageTemplate;

    @MessageMapping("/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto chatMessageDto){
        System.out.println("여기 들어오는중");
        System.out.println(chatMessageDto.getMessage());
        messageTemplate.convertAndSend("/topic/"+roomId, chatMessageDto);
        chatService.saveMessage(roomId, chatMessageDto);
    }



}
