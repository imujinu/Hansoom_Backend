package com.beyond.HanSoom.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageRedisDto {
    private String roomId;
    private Long senderId;
    private String message;
    private long timestamp;
}
