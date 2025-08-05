package com.beyond.HanSoom.review.dto;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.review.domain.Review;
import com.beyond.HanSoom.user.domain.User;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCreateReqDto {
    @NotNull(message = "별점이 비어있습니다.")
    private BigDecimal rating;
    private String contents;
    @NotNull(message = "호텔Id가 비어있습니다.")
    private Long hotelId;
    @NotNull(message = "예약Id가 비어있습니다.")
    private Long reservationId;
    @Builder.Default
    private List<MultipartFile> reviewImages = new ArrayList<>();

    public Review toEntity(User user, Hotel hotel, Reservation reservation) {
        return Review.builder()
                .rating(this.rating)
                .contents(this.contents)
                .user(user)
                .hotel(hotel)
                .reservation(reservation)
                .build();
    }
}
