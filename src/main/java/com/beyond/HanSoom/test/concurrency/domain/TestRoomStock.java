package com.beyond.HanSoom.test.concurrency.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestRoomStock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roomId; // 실제 Room ID와 매핑 가능하도록

    private int stock;

    @Version
    private Long version;

    public void decreaseStock() {
        if (this.stock <= 0) {
            throw new RuntimeException("재고가 부족합니다.");
        }
        this.stock -= 1;
    }

    public void resetStock(int stock) {
        this.stock = stock;
    }
}
