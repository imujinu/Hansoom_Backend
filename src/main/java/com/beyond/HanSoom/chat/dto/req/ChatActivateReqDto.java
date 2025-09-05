package com.beyond.HanSoom.chat.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatActivateReqDto {
    private List<Long> ids;
    private String isActive;
}
