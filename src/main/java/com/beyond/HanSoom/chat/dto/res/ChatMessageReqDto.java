package com.beyond.HanSoom.chat.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageReqDto {
    private Long roomId;
    private String senderEmail;
    private String content; // 암호화된 메시지
    private String timestamp;
    private Long remaining;
    private boolean isWaring;
    public void addWaring(Long remaining){
        this.isWaring = true;
        this.remaining = remaining;
    }


}

