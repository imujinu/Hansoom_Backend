package com.beyond.HanSoom.test.payment.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pay_test_stock")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayTestStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int totalStock;
    private int availableStock;
    private int confirmedCount;

    @Version
    private Long version;

    public void occupyStock() {
        if (this.availableStock <= 0) {
            throw new RuntimeException("재고 부족");
        }
        this.availableStock -= 1;
    }

    public void releaseStock() {
        this.availableStock += 1;
    }

    public void confirmOccupied() {
        this.confirmedCount += 1;
    }
}
