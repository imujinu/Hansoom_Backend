package com.beyond.HanSoom.test.payment.repository;

import com.beyond.HanSoom.test.payment.domain.PayTestStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PayTestStockRepository extends JpaRepository<PayTestStock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from PayTestStock s where s.id = :id")
    Optional<PayTestStock> findByIdWithPessimisticLock(@Param("id") Long id);

    // 버전 충돌 없이 원자적 카운터 증가 (Phase 3 공통)
    @Modifying
    @Query("UPDATE PayTestStock s SET s.confirmedCount = s.confirmedCount + 1 WHERE s.id = :id")
    void incrementConfirmedCount(@Param("id") Long id);

    // 원자적 재고 원복 (Phase 3 실패 시 JPA 전략용)
    @Modifying
    @Query("UPDATE PayTestStock s SET s.availableStock = s.availableStock + 1 WHERE s.id = :id")
    void incrementAvailableStock(@Param("id") Long id);
}
