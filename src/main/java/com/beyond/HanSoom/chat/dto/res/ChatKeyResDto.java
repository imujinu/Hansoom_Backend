package com.beyond.HanSoom.chat.dto.res;


import com.beyond.HanSoom.chat.domain.ChatParticipant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class ChatKeyResDto {
    private String Aes;
    private String iv;

    public ChatKeyResDto fromEntity(ChatParticipant chatParticipant){
        return ChatKeyResDto.builder()
                .Aes(chatParticipant.getAesKey())
                .iv(chatParticipant.getIv())
                .build();
    }
}
