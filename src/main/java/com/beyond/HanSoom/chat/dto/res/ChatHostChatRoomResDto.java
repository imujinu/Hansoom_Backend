package com.beyond.HanSoom.chat.dto.res;

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
public class ChatHostChatRoomResDto {
    private Long id;
    private Long hotelId;
    private String name;
    private String userName;
    private int participantCount;
    private int isOnline;
    private String lastMessage;
    private long unReadCount;
    private String isGroupChat;
    private LocalDateTime timestamp;
    private LocalDateTime createdAt;

    public ChatHostChatRoomResDto fromEntity(ChatRoom chatRoom, ChatMessage chatMessage, int isOnline, Long unReadCount){
        String lastMessage = chatMessage != null ? chatMessage.getContent() : "";
        LocalDateTime timestamp = chatMessage != null ? chatMessage.getCreatedTime() : null;
        return ChatHostChatRoomResDto.builder()
                .id(chatRoom.getId())
                .hotelId(chatRoom.getHotel().getId())
                .name(chatRoom.getHotel().getHotelName()+ " 단체 채팅방")
                .userName("")
                .participantCount(chatRoom.getParticipantList().size())
                .isOnline(isOnline)
                .unReadCount(unReadCount)
                .isGroupChat("Y")
                .lastMessage(lastMessage)
                .timestamp(timestamp)
                .createdAt(chatRoom.getCreatedTime())
                .build();
    }
    public ChatHostChatRoomResDto fromEntity(ChatRoom chatRoom, ChatMessage chatMessage, String userName, Long unReadCount){
        String lastMessage = chatMessage != null ? chatMessage.getContent() : "";
        LocalDateTime timestamp = chatMessage != null ? chatMessage.getCreatedTime() : null;
        return ChatHostChatRoomResDto.builder()
                .id(chatRoom.getId())
                .hotelId(chatRoom.getHotel().getId())
                .name(userName+ "님과의 개인 채팅방")
                .userName(userName)
                .participantCount(chatRoom.getParticipantList().size())
                .isOnline(isOnline)
                .lastMessage(lastMessage)
                .unReadCount(unReadCount)
                .isGroupChat("N")
                .timestamp(timestamp)
                .createdAt(chatRoom.getCreatedTime())
                .build();
    }
}
