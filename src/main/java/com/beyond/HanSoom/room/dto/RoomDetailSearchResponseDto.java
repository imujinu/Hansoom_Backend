package com.beyond.HanSoom.room.dto;

import com.beyond.HanSoom.room.domain.Room;
import com.beyond.HanSoom.roomImage.dto.RoomImageResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RoomDetailSearchResponseDto {
    private Long id;
    private String type;
    private int roomCount;
    private String roomOption1;
    private String roomOption2;
    private String description;
    private int price;
    private int standardPeople;
    private int maximumPeople;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private List<RoomImageResponseDto> roomImages;
    private int remainRoomCount;

    public static RoomDetailSearchResponseDto fromEntity(Room room, List<RoomImageResponseDto> dto, int remainRoomCount, int price) {
        return RoomDetailSearchResponseDto.builder()
                .id(room.getId())
                .type(room.getType())
                .roomCount(room.getRoomCount())
                .roomOption1(room.getRoomOption1())
                .roomOption2(room.getRoomOption2())
                .description(room.getDescription())
                .price(price)
                .standardPeople(room.getStandardPeople())
                .maximumPeople(room.getMaximumPeople())
                .checkIn(room.getCheckIn())
                .checkOut(room.getCheckOut())
                .roomImages(dto)
                .remainRoomCount(remainRoomCount)
                .build();
    }
}
