package com.beyond.HanSoom.chat.controller;

import com.beyond.HanSoom.chat.service.ChatPublishService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

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
