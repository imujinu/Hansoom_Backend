package com.beyond.HanSoom.reply.dto;

import com.beyond.HanSoom.reviewImage.domain.ReviewImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelReviewImageListResDto {
    private String reviewImageUrl;

    public static HotelReviewImageListResDto fromEntity(ReviewImage reviewImage) {
        return HotelReviewImageListResDto.builder()
                .reviewImageUrl(reviewImage.getReviewImageUrl())
                .build();
    }
}
