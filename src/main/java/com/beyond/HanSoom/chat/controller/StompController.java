package com.beyond.HanSoom.chat.controller;

import com.beyond.HanSoom.chat.dto.ChatMessageReqDto;
import com.beyond.HanSoom.chat.service.ChatPublishService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class StompController {
    private final ChatPublishService chatPublishService;
    @MessageMapping("/{roomId}")
    public void sendMessage(@RequestBody ChatMessageReqDto dto){
        System.out.println("=======paylod========");
        System.out.println(dto);
        chatPublishService.publish(dto);
//        System.out.println(chatMessageDto.getMessage());

    }





}
