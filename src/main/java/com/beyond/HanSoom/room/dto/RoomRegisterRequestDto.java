package com.beyond.HanSoom.room.dto;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.domain.HotelState;
import com.beyond.HanSoom.room.domain.Room;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RoomRegisterRequestDto {
    private String type;
    private int roomCount;
    private String roomOption1;
    private String roomOption2;
    private String description;
    private int weekPrice;
    private int weekendPrice;
    private int standardPeople;
    private int maximumPeople;
    private LocalTime checkIn;
    private LocalTime checkOut;

    public Room toEntity(Hotel hotel) {
        return Room.builder()
                .type(this.type)
                .roomCount(this.roomCount)
                .roomOption1(this.roomOption1)
                .roomOption2(this.roomOption2)
                .description(this.description)
                .weekPrice(this.weekPrice)
                .weekendPrice(this.weekendPrice)
                .standardPeople(this.standardPeople)
                .maximumPeople(this.maximumPeople)
                .checkIn(this.checkIn)
                .checkOut(this.checkOut)
                .state(HotelState.WAIT)
                .hotel(hotel)
                .build();
    }
}
