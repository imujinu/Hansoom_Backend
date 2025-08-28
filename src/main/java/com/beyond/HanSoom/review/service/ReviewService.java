package com.beyond.HanSoom.review.service;

import com.beyond.HanSoom.common.service.S3Uploader;
import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.repository.HotelRepository;
import com.beyond.HanSoom.reply.dto.HotelReviewImageListResDto;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.repository.ReservationRepository;
import com.beyond.HanSoom.review.domain.HotelReviewSummary;
import com.beyond.HanSoom.review.domain.Review;
import com.beyond.HanSoom.review.domain.ReviewState;
import com.beyond.HanSoom.review.dto.*;
import com.beyond.HanSoom.review.repository.HotelReviewSummaryRepository;
import com.beyond.HanSoom.review.repository.ReviewRepository;
import com.beyond.HanSoom.reviewImage.domain.ReviewImage;
import com.beyond.HanSoom.reviewImage.repository.ReviewImageRepository;
import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final HotelReviewSummaryRepository hotelReviewSummaryRepository;
    private final ReviewImageRepository reviewImageRepository;

    // 리뷰작성
    public Long createReview(ReviewCreateReqDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));
        Hotel hotel = hotelRepository.findById(dto.getHotelId()).orElseThrow(() -> new EntityNotFoundException("없는 호텔입니다."));
        Reservation reservation = reservationRepository.findById(dto.getReservationId()).orElseThrow(() -> new EntityNotFoundException("없는 예약입니다."));

        Review review = dto.toEntity(user, hotel, reservation);

        if(!dto.getReviewImages().isEmpty()) {
            for(MultipartFile imageFile : dto.getReviewImages()) {
                String imageUrl = s3Uploader.upload(imageFile, "review");
                ReviewImage reviewImage = ReviewImage.builder()
                        .reviewImageUrl(imageUrl)
                        .review(review)
                        .build();
                review.getReviewImageList().add(reviewImage);
            }
        }
        reviewRepository.save(review);
        // 리뷰 rating 합산
        HotelReviewSummary hotelReviewSummary = hotelReviewSummaryRepository.findByHotel(hotel);
        hotelReviewSummary.addReviewRating(dto.getRating());

        log.info("[HANSOOM][INFO] - ReviewService/createReview - 리뷰작성 성공, id={}", review.getId());

        return review.getId();
    }

    // 사용자가 작성한 모든 리뷰목록 (User)
    public Page<ReviewListResDto> getUserReviews(Pageable pageable) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));
        Page<Review> reviewPage = reviewRepository.findByUserAndState(pageable, user, ReviewState.NORMAL);
        Page<ReviewListResDto> reviewListResDtoPage = reviewPage.map(a -> ReviewListResDto.fromEntity(a, a.getReservation()));
        
        log.info("[HANSOOM][INFO] - ReviewService/getUserReviews - 사용자 리뷰목록 출력 성공, email={}", email);
        
        return reviewListResDtoPage;
    }

    // 호텔의 모든 리뷰목록 (Hotel)
    public Page<ReviewListResDto> getHotelReviews(Pageable pageable, Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new EntityNotFoundException("없는 호텔입니다."));
        Page<Review> reviewPage = reviewRepository.findByHotelAndState(pageable, hotel, ReviewState.NORMAL);

        Page<ReviewListResDto> reviewListResDtoPage = reviewPage.map(a -> ReviewListResDto.fromEntity(a, a.getReservation()));

        log.info("[HANSOOM][INFO] - ReviewService/getHotelReviews - 호텔 리뷰목록 출력 성공, hotelId={}", hotelId);

        return reviewListResDtoPage;
    }

    // 리뷰 상세
    public ReviewDetailResDto getDetailReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElseThrow(() -> new EntityNotFoundException("없는 리뷰입니다."));
        return ReviewDetailResDto.fromEntity(review);
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
        if(!dto.getNewImages().isEmpty()) {
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
        }

        log.info("[HANSOOM][INFO] - ReviewService/updateReview - 리뷰수정 성공, id={}", review.getId());
    }

    // 리뷰삭제
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("없는 리뷰입니다."));
        review.deleteReview();
        
        log.info("[HANSOOM][INFO] - ReviewService/deleteReview - 리뷰삭제 성공, id={}", id);
    }

    // HotelReviewSummary
    // 리뷰 평균 및 개수 반환
    public ReviewRatingsResDto getReviewRatings(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new EntityNotFoundException("없는 호텔입니다."));
        HotelReviewSummary hotelReviewSummary = hotelReviewSummaryRepository.findByHotel(hotel);
        ReviewRatingsResDto resDto = ReviewRatingsResDto.fromEntity(hotelReviewSummary);

        log.info("[HANSOOM][INFO] - ReviewService/getReviewRatings - 리뷰 합산데이터 조회 성공, hotelId={}", hotelId);
        
        return resDto;
    }

    // ReviewImage
    // 해당 호텔의 리뷰 중 사진만 모두 가져오기
    public List<HotelReviewImageListResDto> getReviewList(Long hotelId) {
        List<HotelReviewImageListResDto> hotelReviewImageList = reviewImageRepository.findImagesByHotelIdAndReviewNormal(hotelId).stream()
                .map(a -> HotelReviewImageListResDto.fromEntity(a)).toList();
        return hotelReviewImageList;
    }
}
