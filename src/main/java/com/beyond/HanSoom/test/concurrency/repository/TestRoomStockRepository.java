package com.beyond.HanSoom.test.concurrency.repository;

import com.beyond.HanSoom.test.concurrency.domain.TestRoomStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TestRoomStockRepository extends JpaRepository<TestRoomStock, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from TestRoomStock s where s.id = :id")
    Optional<TestRoomStock> findByIdWithPessimisticLock(@Param("id") Long id);
}
