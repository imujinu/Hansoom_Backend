package com.beyond.HanSoom.reservation.dto.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueueReqDto {
    private String hotelId;
    private String roomId;
    private String userId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkInDate;
    public QueueReqDto makeDto(String hotelId, String roomId, LocalDate checkInDate, String userId){
        return QueueReqDto.builder()
                .hotelId(hotelId)
                .roomId(roomId)
                .userId(userId)
                .checkInDate(checkInDate)
                .build();
    }
}
