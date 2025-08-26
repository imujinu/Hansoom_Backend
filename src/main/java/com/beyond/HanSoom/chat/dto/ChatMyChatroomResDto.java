package com.beyond.HanSoom.chat.dto;

import com.beyond.HanSoom.chat.domain.ChatRoom;
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
    private Long unReadCount;
    private Long participants;

    public ChatMyChatroomResDto fromEntity(ChatRoom chatRoom, Long unReadCount){
        return ChatMyChatroomResDto.builder()
                .roomId(chatRoom.getId())
                .hotelName(chatRoom.getHotel().getHotelName())
                .isGroupChat(chatRoom.getIsGroupChat())
                .unReadCount(unReadCount)
                .build();
    }
}
