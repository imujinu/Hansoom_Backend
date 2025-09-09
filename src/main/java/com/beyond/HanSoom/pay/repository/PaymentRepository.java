package com.beyond.HanSoom.pay.repository;

import com.beyond.HanSoom.pay.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Payment findByReservationId(Long id);
}
