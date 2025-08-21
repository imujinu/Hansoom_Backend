package com.beyond.HanSoom.room.dto;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.domain.HotelState;
import com.beyond.HanSoom.room.domain.Room;
import com.beyond.HanSoom.roomImage.domain.RoomImage;
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
public class RoomDetailResponseDto {
    private Long roomId;
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
    private List<RoomImageResponseDto> roomImages;
    private int remainRoomCount;
    private HotelState state;

    public static RoomDetailResponseDto fromEntity(Room room, List<RoomImageResponseDto> dto, int remainRoomCount) {
        return RoomDetailResponseDto.builder()
                .roomId(room.getId())
                .type(room.getType())
                .roomCount(room.getRoomCount())
                .roomOption1(room.getRoomOption1())
                .roomOption2(room.getRoomOption2())
                .description(room.getDescription())
                .weekPrice(room.getWeekPrice())
                .weekendPrice(room.getWeekendPrice())
                .standardPeople(room.getStandardPeople())
                .maximumPeople(room.getMaximumPeople())
                .checkIn(room.getCheckIn())
                .checkOut(room.getCheckOut())
                .roomImages(dto)
                .remainRoomCount(remainRoomCount)
                .state(room.getState())
                .build();
    }
}
