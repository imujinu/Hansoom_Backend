package com.beyond.HanSoom.room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RoomUpdateDto {
    private Long roomId; // 수정할 대상 Room 식별자
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
    private String roomKey;
}
