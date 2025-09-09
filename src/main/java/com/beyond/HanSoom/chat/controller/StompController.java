package com.beyond.HanSoom.chat.controller;

import com.beyond.HanSoom.chat.dto.res.ChatMessageReqDto;
import com.beyond.HanSoom.chat.service.ChatPublishService;
import com.beyond.HanSoom.chat.service.ChatRateLimiter;
import com.beyond.HanSoom.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequiredArgsConstructor
public class StompController {
    private final ChatPublishService chatPublishService;
    private final UserRepository userRepository;
    private final ChatRateLimiter chatRateLimiter;
    @MessageMapping("/{roomId}")
    public void sendMessage(@RequestBody ChatMessageReqDto dto){
        System.out.println("=======paylod========");
        System.out.println(dto);

        if (!chatRateLimiter.canSendMessage(dto.getSenderEmail())) {
            long remaining = chatRateLimiter.getBlockedRemaining(dto.getSenderEmail());
            // 사용자에게 경고 메시지 전송
            dto.addWaring(remaining);
          chatPublishService.publish(dto);
        }
        chatPublishService.publish(dto);
//        System.out.println(chatMessageDto.getMessage());

    }





}
