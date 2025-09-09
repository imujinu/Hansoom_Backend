package com.beyond.HanSoom.chat.dto.req;

import com.beyond.HanSoom.chat.domain.ChatAnnouncement;
import com.beyond.HanSoom.chat.domain.ChatRoom;
import com.beyond.HanSoom.hotel.domain.Hotel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatAnnouncementReqDto {
    private long hotelId;
    private String title;
    private String content;
    private String isActive;
    private String chatType;
    private LocalDateTime createdAt;

    public ChatAnnouncement toEntity(ChatAnnouncementReqDto dto, Hotel hotel){
        return ChatAnnouncement.builder()
                .hotel(hotel)
                .title(dto.getTitle())
                .content(dto.getContent())
                .isActive(dto.getIsActive())
                .chatType(dto.getChatType())
                .build();
    }


}
