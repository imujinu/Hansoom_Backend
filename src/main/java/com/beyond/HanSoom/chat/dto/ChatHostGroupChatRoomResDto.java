package com.beyond.HanSoom.chat.dto;

import com.beyond.HanSoom.chat.domain.ChatMessage;
import com.beyond.HanSoom.chat.domain.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ChatHostGroupChatRoomResDto {
    private Long id;
    private String name;
    private int participantCount;
    private String lastMessage;
    private LocalDateTime timestamp;
    private LocalDateTime createdAt;

    public ChatHostGroupChatRoomResDto fromEntity(ChatRoom chatRoom, ChatMessage message){
        return ChatHostGroupChatRoomResDto.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getHotel().getHotelName()+ " 단체 채팅방")
                .participantCount(chatRoom.getParticipantList().size())
                .lastMessage(message.getContent())
                .timestamp(message.getCreatedTime())
                .createdAt(chatRoom.getCreatedTime())
                .build();
    }
}
