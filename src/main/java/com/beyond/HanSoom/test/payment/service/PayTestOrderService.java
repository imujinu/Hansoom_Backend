package com.beyond.HanSoom.test.payment.service;

import com.beyond.HanSoom.test.payment.domain.PayTestOrder;
import com.beyond.HanSoom.test.payment.domain.PayTestOrderState;
import com.beyond.HanSoom.test.payment.domain.PayTestStock;
import com.beyond.HanSoom.test.payment.repository.PayTestOrderRepository;
import com.beyond.HanSoom.test.payment.repository.PayTestStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PayTestOrderService {

    private final PayTestStockRepository stockRepository;
    private final PayTestOrderRepository orderRepository;

    // 낙관락·ReentrantLock 전략의 Phase 1: DB 재고 차감 + PENDING 주문 생성
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long occupyStockAndCreateOrder(Long stockId, Long userId) {
        PayTestStock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + stockId));
        stock.occupyStock();
        PayTestOrder order = PayTestOrder.builder()
                .stockId(stockId)
                .userId(userId)
                .state(PayTestOrderState.PENDING)
                .build();
        return orderRepository.save(order).getId();
    }

    // 비관락 전략의 Phase 1: SELECT FOR UPDATE → 점유 후 즉시 커밋 → 락 범위 최소화
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long occupyStockWithPessimisticLockAndCreateOrder(Long stockId, Long userId) {
        PayTestStock stock = stockRepository.findByIdWithPessimisticLock(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found: " + stockId));
        stock.occupyStock();
        PayTestOrder order = PayTestOrder.builder()
                .stockId(stockId)
                .userId(userId)
                .state(PayTestOrderState.PENDING)
                .build();
        return orderRepository.save(order).getId();
    }

    // Redis+Lua 전략의 Phase 1: Redis가 재고를 관리하므로 DB는 주문만 기록
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long createPendingOrder(Long stockId, Long userId) {
        PayTestOrder order = PayTestOrder.builder()
                .stockId(stockId)
                .userId(userId)
                .state(PayTestOrderState.PENDING)
                .build();
        return orderRepository.save(order).getId();
    }

    // Phase 3 성공: 모든 전략 공통
    // @Modifying UPDATE로 confirmedCount를 원자적으로 증가해 @Version 충돌 방지
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void confirmOrder(Long orderId, Long stockId) {
        PayTestOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.changeState(PayTestOrderState.CONFIRMED);
        stockRepository.incrementConfirmedCount(stockId);
    }

    // Phase 3 실패: JPA 전략용 — DB 재고 원복
    // @Modifying UPDATE로 availableStock을 원자적으로 복구해 @Version 충돌 방지
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failOrder(Long orderId, Long stockId) {
        PayTestOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.changeState(PayTestOrderState.FAILED);
        stockRepository.incrementAvailableStock(stockId);
    }

    // Phase 3 실패: Redis 전략용 — DB 재고는 건드리지 않음 (Redis가 권한)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void failOrderOnly(Long orderId) {
        PayTestOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        order.changeState(PayTestOrderState.FAILED);
    }

    // Redis 전략에서 Lua 스크립트의 maxStock 파라미터로 사용
    @Transactional(readOnly = true)
    public Long getTotalStock(Long stockId) {
        return stockRepository.findById(stockId)
                .map(s -> (long) s.getTotalStock())
                .orElse(null);
    }
}
