package com.beyond.HanSoom.chat.dto.res;

import com.beyond.HanSoom.user.domain.User;
import lombok.*;

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



    public void updateUser(User user) {
        this.senderName=user.getName();
        this.profileImage=user.getProfileImage();
    }
}
