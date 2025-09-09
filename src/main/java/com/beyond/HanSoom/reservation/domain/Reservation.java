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
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
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
    private State state = State.PENDING;




    //PrePersit는 값이 DB에 저장되기 전 JPA가 감지하고 실행시켜주는 어노테이션

    @PrePersist
    public void insertUUID(){
        this.uuid = UUID.randomUUID().toString();
    }

    public void changeState(State state){
        this.state = state;
    }
    public void stateFailed(){
        this.state= State.FAILED;
    }

    public void updateCacheSet(User user, Hotel hotel , Room room){
        this.user= user;
        this.hotel =hotel;
        this.room =room;

    }
}
