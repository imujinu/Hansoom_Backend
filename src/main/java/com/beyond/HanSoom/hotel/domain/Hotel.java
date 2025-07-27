package com.beyond.HanSoom.hotel.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Entity
public class Hotel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String hotelName;
    private String address;
    private String phoneNumber;
    private String image;
    private String describtion;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private HotelState state = HotelState.WAIT;
    @Enumerated(EnumType.STRING)
    private HotelType type;
    private LocalDateTime answerTime;
}
