package com.beyond.HanSoom.chat.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatCreateReqDto {
    private Long reservationId;
    private String aesKey;
    private String iv;
}
