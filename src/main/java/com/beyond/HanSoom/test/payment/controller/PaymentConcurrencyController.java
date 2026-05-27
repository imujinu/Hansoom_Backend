package com.beyond.HanSoom.test.payment.controller;

import com.beyond.HanSoom.test.payment.domain.PayTestOrder;
import com.beyond.HanSoom.test.payment.domain.PayTestOrderState;
import com.beyond.HanSoom.test.payment.domain.PayTestStock;
import com.beyond.HanSoom.test.payment.dto.PayTestResetResDto;
import com.beyond.HanSoom.test.payment.dto.PayTestStatusResDto;
import com.beyond.HanSoom.test.payment.repository.PayTestOrderRepository;
import com.beyond.HanSoom.test.payment.repository.PayTestStockRepository;
import com.beyond.HanSoom.test.payment.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test/pay-reservations")
@RequiredArgsConstructor
public class PaymentConcurrencyController {

    private final OptimisticPaymentService optimisticService;
    private final PessimisticPaymentService pessimisticService;
    private final ReentrantPaymentService reentrantService;
    private final RedisLuaPaymentService redisLuaService;
    private final PayTestStockRepository stockRepository;
    private final PayTestOrderRepository orderRepository;

    @PostMapping("/{strategy}")
    public ResponseEntity<Map<String, String>> reserve(
            @PathVariable String strategy,
            @RequestParam Long stockId,
            @RequestParam Long userId,
            @RequestParam(defaultValue = "500") long paymentDelayMs) {

        PaymentConcurrencyService service = switch (strategy.toLowerCase()) {
            case "optimistic"  -> optimisticService;
            case "pessimistic" -> pessimisticService;
            case "reentrant"   -> reentrantService;
            case "redis"       -> redisLuaService;
            default -> throw new IllegalArgumentException("Unknown strategy: " + strategy);
        };

        try {
            PayTestOrderState result = service.reserve(stockId, userId, paymentDelayMs);
            return ResponseEntity.ok(Map.of("result", result.name()));
        } catch (PessimisticLockingFailureException e) {
            return ResponseEntity.ok(Map.of("result", PayTestOrderState.FAILED.name()));
        }
    }

    @PostMapping("/reset")
    @Transactional
    public ResponseEntity<PayTestResetResDto> reset(@RequestParam int stockCount) {
        // 기존 stock의 Redis key를 모두 정리한 뒤 삭제
        stockRepository.findAll().forEach(s -> redisLuaService.resetRedisKey(s.getId()));
        orderRepository.deleteAllInBatch();
        stockRepository.deleteAllInBatch();

        PayTestStock stock = stockRepository.save(PayTestStock.builder()
                .totalStock(stockCount)
                .availableStock(stockCount)
                .confirmedCount(0)
                .build());

        redisLuaService.resetRedisKey(stock.getId());

        return ResponseEntity.ok(new PayTestResetResDto(
                stock.getId(),
                "pay-test:stock:" + stock.getId(),
                stockCount
        ));
    }

    @GetMapping("/status/{stockId}")
    public ResponseEntity<PayTestStatusResDto> status(@PathVariable Long stockId) {
        PayTestStock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + stockId));

        long pending   = orderRepository.countByStockIdAndState(stockId, PayTestOrderState.PENDING);
        long confirmed = orderRepository.countByStockIdAndState(stockId, PayTestOrderState.CONFIRMED);
        long failed    = orderRepository.countByStockIdAndState(stockId, PayTestOrderState.FAILED);
        long redis     = redisLuaService.getRedisOccupied(stockId);

        return ResponseEntity.ok(new PayTestStatusResDto(
                stock.getTotalStock(),
                stock.getAvailableStock(),
                stock.getConfirmedCount(),
                pending,
                confirmed,
                failed,
                redis
        ));
    }
}
