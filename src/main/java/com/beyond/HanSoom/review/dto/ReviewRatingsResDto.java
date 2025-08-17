package com.beyond.HanSoom.review.dto;

import com.beyond.HanSoom.review.domain.HotelReviewSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRatingsResDto {
    private BigDecimal averageRating;
    private int ratingCount;

    public static ReviewRatingsResDto fromEntity(HotelReviewSummary hotelReviewSummary) {
        return ReviewRatingsResDto.builder()
                .averageRating(hotelReviewSummary.getAverage())
                .ratingCount(hotelReviewSummary.getRatingCount())
                .build();
    }
}
