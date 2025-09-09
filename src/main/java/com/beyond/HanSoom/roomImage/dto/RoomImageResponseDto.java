package com.beyond.HanSoom.roomImage.dto;

import com.beyond.HanSoom.roomImage.domain.RoomImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RoomImageResponseDto {
    private Long id;
    private String imageUrl;

    public static RoomImageResponseDto fromEntity(RoomImage roomImage) {
        return RoomImageResponseDto.builder()
                .id(roomImage.getId())
                .imageUrl(roomImage.getImageUrl())
                .build();
    }
}
