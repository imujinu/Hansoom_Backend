package com.beyond.HanSoom.test.payment.service;

import com.beyond.HanSoom.test.payment.domain.PayTestOrderState;

public interface PaymentConcurrencyService {
    PayTestOrderState reserve(Long stockId, Long userId, long paymentDelayMs);
}
