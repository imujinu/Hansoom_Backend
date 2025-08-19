package com.beyond.HanSoom.chat.dto;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResDto {
    private Long roomId;
    private String content;
    private String timestamp;
    private String senderEmail;
    private String senderName;
}
