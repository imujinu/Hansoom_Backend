package com.beyond.HanSoom.room.domain;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.domain.HotelState;
import com.beyond.HanSoom.room.dto.RoomUpdateDto;
import com.beyond.HanSoom.roomImage.domain.RoomImage;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Entity
@Builder
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String type;
    private int roomCount;
    private String roomOption1;
    private String roomOption2;
    private String describtion;
    private int weekPrice;
    private int weekendPrice;
    private int standardPeople;
    private int maximumPeople;
    private LocalTime checkIn;
    private LocalTime checkOut;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private HotelState state = HotelState.WAIT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @Builder.Default
    @OneToMany(mappedBy = "room", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<RoomImage> roomImages = new ArrayList<>();

    @Transient
    private List<String> imageUrlsMarkedForDeletion = new ArrayList<>();

    public void updateState(HotelState state) {
        this.state = state;
    }

    public void updateInfo(RoomUpdateDto dto) {
        this.type = dto.getType();
        this.roomCount = dto.getRoomCount();
        this.weekPrice = dto.getWeekPrice();
        this.weekendPrice = dto.getWeekendPrice();
        this.standardPeople = dto.getStandardPeople();
        this.maximumPeople = dto.getMaximumPeople();
        this.roomOption1 = dto.getRoomOption1();
        this.roomOption2 = dto.getRoomOption2();
        this.checkIn = dto.getCheckIn();
        this.checkOut = dto.getCheckOut();
        this.describtion = dto.getDescribtion();
    }

    public void markImagesForDeletion() {
        this.imageUrlsMarkedForDeletion = this.roomImages.stream()
                .map(RoomImage::getImageUrl)
                .collect(Collectors.toList());
    }

    public boolean hasImagesMarkedForDeletion() {
        return !imageUrlsMarkedForDeletion.isEmpty();
    }

    public void updateRoomImages(List<RoomImage> roomImageList) {
        this.roomImages = roomImageList;
    }
}
