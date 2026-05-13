package com.beyond.HanSoom.test.concurrency.service;

import com.beyond.HanSoom.test.concurrency.domain.TestReservation;
import com.beyond.HanSoom.test.concurrency.domain.TestRoomStock;
import com.beyond.HanSoom.test.concurrency.repository.TestReservationRepository;
import com.beyond.HanSoom.test.concurrency.repository.TestRoomStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TestReservationService {
    private final TestRoomStockRepository stockRepository;
    private final TestReservationRepository reservationRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reserveWithNewTransaction(Long stockId, Long userId) {
        TestRoomStock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found"));
        
        stock.decreaseStock();
        
        TestReservation reservation = TestReservation.builder()
                .testRoomStockId(stockId)
                .userId(userId)
                .build();
        reservationRepository.save(reservation);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reserveWithPessimisticLock(Long stockId, Long userId) {
        TestRoomStock stock = stockRepository.findByIdWithPessimisticLock(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found"));
        
        stock.decreaseStock();
        
        TestReservation reservation = TestReservation.builder()
                .testRoomStockId(stockId)
                .userId(userId)
                .build();
        reservationRepository.save(reservation);
    }
}
