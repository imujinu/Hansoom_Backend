package com.beyond.HanSoom.reservation.domain;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.room.domain.Room;
import com.beyond.HanSoom.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Reservation {
    @Id
    @GeneratedValue(strategy= GenerationType.UUID)
    private UUID id;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Column(nullable = false)
    private User user;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    @Column(nullable = false)
    private Hotel hotel;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    @Column(nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private LocalDateTime reservationDate;

    @Column(nullable = false)
    private long people;

    private String request;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private State state = State.RESERVE;

    public void cancel(){
        this.state = State.CANCEL;
    }
}
