package com.beyond.HanSoom.review.dto;

import com.beyond.HanSoom.review.domain.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDetailResDto {
    private BigDecimal rating;
    private String contents;
    private LocalDateTime createdTime;
    @Builder.Default
    private List<ReviewImageResDto> reviewImageResDtoList = new ArrayList<>();

    public static ReviewDetailResDto fromEntity(Review review) {
        return ReviewDetailResDto.builder()
                .rating(review.getRating())
                .contents(review.getContents())
                .createdTime(review.getCreatedTime())
                .reviewImageResDtoList(review.getReviewImageDtoList())
                .build();
    }
}
