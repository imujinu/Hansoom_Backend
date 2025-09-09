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
    private String myPublicKey;
    private String myPrivateKey;
    private String hostPublicKey;

    public ChatKeyResDto fromEntity(ChatParticipant me, ChatParticipant host){
        return ChatKeyResDto.builder()
                .myPublicKey(me.getPublicKey())
                .myPrivateKey(me.getPrivateKey())
                .hostPublicKey(host.getPublicKey())
                .build();
    }
}
