package com.beyond.HanSoom.chat.dto.req;

import com.beyond.HanSoom.chat.domain.ChatAnnouncement;
import com.beyond.HanSoom.chat.domain.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatAnnouncementReqDto {
    private long chatRoomId;
    private String title;
    private String content;
    private String isActive;

    public ChatAnnouncement toEntity(ChatAnnouncementReqDto dto, ChatRoom chatRoom){
        return ChatAnnouncement.builder()
                .chatRoom(chatRoom)
                .title(dto.title)
                .content(dto.content)
                .isActive(dto.isActive)
                .build();
    }
}
