package com.beyond.HanSoom.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageReqDto {
    private String roomId;
    private String senderEmail;
    private String content;
    private String timestamp;
}
