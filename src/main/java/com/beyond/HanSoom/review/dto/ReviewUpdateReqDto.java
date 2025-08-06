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
public class ReviewUpdateReqDto {
    @NotNull(message = "리뷰Id가 비어있습니다.")
    private Long id;
    @NotNull(message = "별점이 비어있습니다.")
    private BigDecimal rating;
    private String contents;
    private List<String> existingImageUrls = new ArrayList<>();
    private List<MultipartFile> newImages = new ArrayList<>();
}
