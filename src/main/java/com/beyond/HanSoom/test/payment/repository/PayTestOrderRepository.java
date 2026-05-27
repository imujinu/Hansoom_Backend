package com.beyond.HanSoom.test.payment.repository;

import com.beyond.HanSoom.test.payment.domain.PayTestOrder;
import com.beyond.HanSoom.test.payment.domain.PayTestOrderState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PayTestOrderRepository extends JpaRepository<PayTestOrder, Long> {

    long countByStockIdAndState(Long stockId, PayTestOrderState state);

    void deleteAllByStockId(Long stockId);
}
