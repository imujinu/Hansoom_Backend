package com.beyond.HanSoom.reservation.domain;

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
    private String payment_type;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private State state;

    @Column(nullable = false)
    private LocalDateTime paymentTime;

    @Column(nullable = false)
    private LocalDateTime cancelTime;


}
