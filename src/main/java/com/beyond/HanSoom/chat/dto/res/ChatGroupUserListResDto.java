package com.beyond.HanSoom.chat.dto.res;

import com.beyond.HanSoom.chat.domain.ChatParticipant;
import com.beyond.HanSoom.user.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ChatGroupUserListResDto {
    private Long id;
    private String name;
    private UserRole role;
    private String isOnline;
    private String avatar;

    public ChatGroupUserListResDto fromEntity(ChatParticipant chatParticipant){
        return ChatGroupUserListResDto.builder()
                .id(chatParticipant.getId())
                .name(chatParticipant.getUser().getName())
                .role(chatParticipant.getUser().getUserRole())
                .isOnline(chatParticipant.getIsOnline())
                .avatar(chatParticipant.getUser().getProfileImage())
                .build();
    }
}
