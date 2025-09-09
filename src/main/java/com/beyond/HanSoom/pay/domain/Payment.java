package com.beyond.HanSoom.pay.domain;

import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.domain.State;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Payment {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false)
    private String paymentType;

    @Column(nullable = false)
    private String price;

    @Enumerated(EnumType.STRING)
    private State state;

    @Column(nullable = false)
    @CreationTimestamp
    private LocalDateTime PaymentTime;

    @UpdateTimestamp
    private LocalDateTime PaymentCancelTime;

}
