package com.beyond.HanSoom.chat.dto.res;

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

    public void updateSenderName(String name){
        this.senderName=name;
    }
}
