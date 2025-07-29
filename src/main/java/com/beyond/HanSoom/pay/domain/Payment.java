package com.beyond.HanSoom.pay.domain;

import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.domain.State;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    @JoinColumn(name = "reserve_id")
    @Column(nullable = false)
    private Reservation reservation;

    @Column(nullable = false)
    private String PaymentType;

    @Column(nullable = false)
    private String price;

    @Column(nullable = false)
    private State state;

    @Column(nullable = false)
    private LocalDateTime PaymentTime;

    private LocalDateTime PaymentCancelTime;

}
