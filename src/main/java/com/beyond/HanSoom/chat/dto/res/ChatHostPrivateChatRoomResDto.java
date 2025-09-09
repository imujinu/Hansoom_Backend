package com.beyond.HanSoom.chat.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatHostPrivateChatRoomResDto {
    private Long id;
    private String guestName;
    private String lastMessage;
    private LocalDateTime timestamp;
    private Long unreadCount;
}
