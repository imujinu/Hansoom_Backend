package com.beyond.HanSoom.chat.dto.res;

import com.beyond.HanSoom.chat.domain.ChatParticipant;
import com.beyond.HanSoom.user.domain.User;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageResDto {
    private Long roomId;
    private String content;
    private String timestamp;
    private String senderEmail;
    private String senderName;
    private String profileImage;
    private Long remaining;
    private boolean isWaring;

    // 암호화 관련 필드 추가
    private Map<String, String> keys; // me, other
    private String iv; // AES 초기화 벡터

    public void updateUser(User user) {
        this.senderName = user.getName();
        this.profileImage = user.getProfileImage();
    }

    public void updateKeySet(ChatParticipant me, ChatParticipant host) {
        Map<String, String> keysMap = new HashMap<>();
        keysMap.put("me", me.getPrivateKey());
        keysMap.put("other", host.getPrivateKey());
        iv = me.getIv();
        this.keys = keysMap;
    }
}
