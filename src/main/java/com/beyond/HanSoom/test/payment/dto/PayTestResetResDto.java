package com.beyond.HanSoom.test.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PayTestResetResDto {
    private Long stockId;
    private String redisKey;
    private int initialStock;
}
