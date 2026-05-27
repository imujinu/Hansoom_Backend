package com.beyond.HanSoom.test.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PayTestStatusResDto {
    private int totalStock;
    private int availableStock;
    private int confirmedCount;
    private long pendingOrderCount;
    private long confirmedOrderCount;
    private long failedOrderCount;
    private long redisOccupied;
}
