package com.beyond.HanSoom.chat.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private Long roomId;
    private Long senderId;
    private String message;
    private Long timestamp;
}
