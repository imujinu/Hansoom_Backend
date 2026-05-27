package com.beyond.HanSoom.test.payment.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pay_test_order")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayTestOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long stockId;
    private Long userId;

    @Enumerated(EnumType.STRING)
    private PayTestOrderState state;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void changeState(PayTestOrderState state) {
        this.state = state;
    }
}
