package com.beyond.HanSoom.review.dto;

import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.review.domain.Review;
import com.beyond.HanSoom.reviewImage.domain.ReviewImage;
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
public class ReviewListResDto {
    private Long id;
    private Long hotelId;
    private String hotelName;
    @Builder.Default
    private String userNickname = "익명사용자";
    private String roomType;
    private BigDecimal rating;
    private String contents;
    private Long replyId;
    private String replyContents;
    private LocalDateTime createdTime;
    @Builder.Default
    private List<ReviewImageResDto> reviewImageResDtoList = new ArrayList<>();

    public static ReviewListResDto fromEntity(Review review, Reservation reservation) {
        return ReviewListResDto.builder()
                .id(review.getId())
                .hotelId(review.getHotel().getId())
                .hotelName(review.getHotel().getHotelName())
                .userNickname(review.getUser().getNickName())
                .roomType(reservation.getRoom().getType())
                .rating(review.getRating())
                .contents(review.getContents())
                .replyId(review.getReply() != null ? review.getReply().getId() : null)
                .replyContents(review.getReply() != null ? review.getReply().getContents() : null)
                .createdTime(review.getCreatedTime())
                .reviewImageResDtoList(review.getReviewImageDtoList())
                .build();
    }
}
