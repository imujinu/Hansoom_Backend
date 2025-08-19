package com.beyond.HanSoom.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMyChatroomResDto {
    private Long roomId;
    private String hotelName;
    private String isGroupChat;
    private Long ueReadCount;
}
