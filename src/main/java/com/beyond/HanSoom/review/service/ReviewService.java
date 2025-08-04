package com.beyond.HanSoom.review.service;

import com.beyond.HanSoom.common.service.S3Uploader;
import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.repository.HotelRepository;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.repository.ReservationRepository;
import com.beyond.HanSoom.review.domain.Review;
import com.beyond.HanSoom.review.dto.ReviewCreateReqDto;
import com.beyond.HanSoom.review.dto.ReviewUpdateReqDto;
import com.beyond.HanSoom.review.repository.ReviewRepository;
import com.beyond.HanSoom.reviewImage.domain.ReviewImage;
import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final ReservationRepository reservationRepository;
    private final S3Uploader s3Uploader;

    // 리뷰작성
    public Long createReview(ReviewCreateReqDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));
        Hotel hotel = hotelRepository.findById(dto.getHotelId()).orElseThrow(() -> new EntityNotFoundException("없는 호텔입니다."));
        Reservation reservation = reservationRepository.findById(dto.getReservationId()).orElseThrow(() -> new EntityNotFoundException("없는 예약입니다."));

        Review review = dto.toEntity(user, hotel, reservation);

        for(MultipartFile imageFile : dto.getReviewImages()) {
            String imageUrl = s3Uploader.upload(imageFile, "review");
            ReviewImage reviewImage = ReviewImage.builder()
                    .reviewImageUrl(imageUrl)
                    .review(review)
                    .build();
            review.getReviewImageList().add(reviewImage);
        }
        reviewRepository.save(review);

        log.info("[HANSOOM][INFO] - ReviewService/createReview - 리뷰작성 성공, id={}", review.getId());

        return review.getId();
    }

    // 리뷰수정
    public void updateReview(ReviewUpdateReqDto dto) {
        Review review = reviewRepository.findById(dto.getId())
                .orElseThrow(() -> new EntityNotFoundException("없는 리뷰입니다."));

        review.updateReview(dto.getRating(), dto.getContents());

        // 기존 이미지 URL 목록
        List<String> existingUrls = dto.getExistingImageUrls();
        // 삭제할 이미지 식별 및 제거
        List<ReviewImage> toRemove = review.getReviewImageList().stream()
                .filter(img -> !existingUrls.contains(img.getReviewImageUrl()))
                .collect(Collectors.toList());

        toRemove.forEach(img -> {
            s3Uploader.delete(img.getReviewImageUrl());
            review.getReviewImageList().remove(img);
        });

        // 새 이미지 파일 업로드 및 추가
        for (MultipartFile file : dto.getNewImages()) {
            if (!file.isEmpty()) {
                String uploadedUrl = s3Uploader.upload(file, "review");
                ReviewImage newImg = ReviewImage.builder()
                        .review(review)
                        .reviewImageUrl(uploadedUrl)
                        .build();
                review.getReviewImageList().add(newImg);
            }
        }

        log.info("[HANSOOM][INFO] - ReviewService/updateReview - 리뷰수정 성공, id={}", review.getId());
    }

    // 리뷰삭제
}
