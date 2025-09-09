package com.beyond.HanSoom.chat.dto.res;

import com.beyond.HanSoom.chat.domain.ChatAnnouncement;
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
public class ChatAnnouncementResDto {
    private Long id;
    private long hotelId;
    private String title;
    private String content;
    private String isActive;
    private String chatType;
    private LocalDateTime createdAt;

    public ChatAnnouncementResDto fromEntity(ChatAnnouncement ch , Hotel hotel){
        return ChatAnnouncementResDto.builder()
                .id(ch.getId())
                .hotelId(hotel.getId())
                .title(ch.getTitle())
                .content(ch.getContent())
                .isActive(ch.getIsActive())
                .chatType(ch.getChatType())
                .createdAt(ch.getCreatedTime())
                .build();
    }
}
