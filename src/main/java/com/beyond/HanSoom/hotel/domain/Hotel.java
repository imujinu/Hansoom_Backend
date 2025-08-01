package com.beyond.HanSoom.hotel.domain;

import com.beyond.HanSoom.room.domain.Room;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private double latitude;
    private double longitude;

    @Builder.Default
    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Room> rooms = new ArrayList<>();

    public void updateState(HotelState state) {
        this.state = state;
        this.answerTime = LocalDateTime.now();
    }

    public void updateBasicInfo(String name, String address, String phone, String desc, HotelType type) {
        this.hotelName = name;
        this.address = address;
        this.phoneNumber = phone;
        this.describtion = desc;
        this.type = type;
    }

    public void updateRooms(List<Room> rooms) {
        this.rooms = rooms;
    }

    public void updateImage(String newHotelImageUrl) {
        this.image = newHotelImageUrl;
    }
}
