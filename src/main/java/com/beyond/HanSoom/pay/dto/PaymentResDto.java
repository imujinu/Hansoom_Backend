package com.beyond.HanSoom.pay.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResDto {
    private Map<String,Object> response;
    private boolean isSuccess;
    private String message;
}
