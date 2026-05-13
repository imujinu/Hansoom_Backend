package com.beyond.HanSoom.test.concurrency.controller;

import com.beyond.HanSoom.test.concurrency.domain.TestRoomStock;
import com.beyond.HanSoom.test.concurrency.repository.TestReservationRepository;
import com.beyond.HanSoom.test.concurrency.repository.TestRoomStockRepository;
import com.beyond.HanSoom.test.concurrency.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test/reservations")
@RequiredArgsConstructor
public class ConcurrencyTestController {

    private final NoLockReservationService noLockService;
    private final PessimisticLockReservationService pessimisticService;
    private final OptimisticLockReservationService optimisticService;
    private final ReentrantLockReservationService reentrantService;
    private final TestRoomStockRepository stockRepository;
    private final TestReservationRepository reservationRepository;

    @PostMapping("/{strategy}")
    public ResponseEntity<String> reserve(
            @PathVariable String strategy,
            @RequestParam Long stockId,
            @RequestParam Long userId) {
        
        ConcurrencyTestService service = switch (strategy.toLowerCase()) {
            case "nolock" -> noLockService;
            case "pessimistic" -> pessimisticService;
            case "optimistic" -> optimisticService;
            case "reentrant" -> reentrantService;
            default -> throw new IllegalArgumentException("Unknown strategy: " + strategy);
        };

        service.reserve(stockId, userId);
        return ResponseEntity.ok("Success");
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> reset(@RequestParam int stockCount) {
        reservationRepository.deleteAll();
        stockRepository.deleteAll();
        
        TestRoomStock stock = TestRoomStock.builder()
                .roomId(1L)
                .stock(stockCount)
                .build();
        TestRoomStock savedStock = stockRepository.save(stock);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Reset success");
        response.put("stockId", savedStock.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{stockId}")
    public ResponseEntity<Map<String, Object>> getStatus(@PathVariable Long stockId) {
        TestRoomStock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found"));
        long reservationCount = reservationRepository.countByTestRoomStockId(stockId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("remainingStock", stock.getStock());
        response.put("reservationCount", reservationCount);
        return ResponseEntity.ok(response);
    }
}
