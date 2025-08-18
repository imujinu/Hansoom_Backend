package com.beyond.HanSoom.chat.controller;

import com.beyond.HanSoom.chat.dto.ChatMessageDto;
import com.beyond.HanSoom.chat.service.ChatPublishService;
import com.beyond.HanSoom.chat.service.ChatService;
import com.beyond.HanSoom.chat.service.ChatStreamListenerService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class StompController {
    private final ChatPublishService chatPublishService;
    @MessageMapping("/{roomId}")
    public void sendMessage(Map<String, String> payload){

        chatPublishService.publish(payload);
//        System.out.println(chatMessageDto.getMessage());

    }





}
