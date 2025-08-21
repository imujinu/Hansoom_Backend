package com.beyond.HanSoom.review.dto;

import com.beyond.HanSoom.reviewImage.domain.ReviewImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewImageResDto {
    private String reviewImageUrl;

    public static ReviewImageResDto fromEntity(ReviewImage reviewImage) {
        return ReviewImageResDto.builder()
                .reviewImageUrl(reviewImage.getReviewImageUrl())
                .build();
    }
}
