package com.beyond.HanSoom.pay.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReqDto {
    private String paymentKey;
    private String orderId;
    private String amount;

}
