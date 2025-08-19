package com.beyond.HanSoom.chat.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResDto {
    private Long roomId;
    private String content;
    private Long timestamp;
    private String senderEmail;
    private String senderName;
}
