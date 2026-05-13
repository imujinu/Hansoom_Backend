package com.beyond.HanSoom.test.concurrency.repository;

import com.beyond.HanSoom.test.concurrency.domain.TestReservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestReservationRepository extends JpaRepository<TestReservation, Long> {
    long countByTestRoomStockId(Long testRoomStockId);
}
