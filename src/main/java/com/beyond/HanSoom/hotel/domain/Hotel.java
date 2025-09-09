package com.beyond.HanSoom.hotel.domain;

import com.beyond.HanSoom.common.domain.BaseTimeEntity;
import com.beyond.HanSoom.review.domain.HotelReviewSummary;
import com.beyond.HanSoom.room.domain.Room;
import com.beyond.HanSoom.user.domain.User;
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
public class Hotel extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String hotelName;
    private String address;
    private String phoneNumber;
    private String image;
    private String description;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private HotelState state = HotelState.WAIT;
    @Enumerated(EnumType.STRING)
    private HotelType type;
    private LocalDateTime answerTime;
    private double latitude;
    private double longitude;
    private long reservationCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Room> rooms = new ArrayList<>();

    @OneToOne(mappedBy = "hotel", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private HotelReviewSummary hotelReviewSummary;

    @PrePersist
    private void ensureSummary() {
        if (this.hotelReviewSummary == null) {
            HotelReviewSummary summary = new HotelReviewSummary();
            summary.setHotel(this);
            this.hotelReviewSummary = summary;
        }
    }


    public void updateState(HotelState state) {
        this.state = state;
        this.answerTime = LocalDateTime.now();
    }

    public void updateBasicInfo(String name, String address, String phone, String desc, HotelType type, Double latitude, Double longitude) {
        this.hotelName = name;
        this.address = address;
        this.phoneNumber = phone;
        this.description = desc;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void updateRooms(List<Room> rooms) {
        this.rooms.clear();
        this.rooms.addAll(rooms);
    }

    public void updateImage(String newHotelImageUrl) {
        this.image = newHotelImageUrl;
    }

    public void updateCount(){
        this.reservationCount++;
    }
}
