package com.beyond.HanSoom.review.service;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.hotel.repository.HotelRepository;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.reservation.repository.ReservationRepository;
import com.beyond.HanSoom.review.domain.Review;
import com.beyond.HanSoom.review.dto.ReviewCreateReqDto;
import com.beyond.HanSoom.review.repository.ReviewRepository;
import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final HotelRepository hotelRepository;
    private final ReservationRepository reservationRepository;

    // 리뷰작성
    public Long createReview(ReviewCreateReqDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));
        Hotel hotel = hotelRepository.findById(dto.getHotelId()).orElseThrow(() -> new EntityNotFoundException("없는 호텔입니다."));
        Reservation reservation = reservationRepository.findById(dto.getReservationId()).orElseThrow(() -> new EntityNotFoundException("없는 예약입니다."));

        // Todo - reviewImages 처리

        Review review = dto.toEntity(user, hotel, reservation);
        reviewRepository.save(review);

        log.info("[HANSOOM][INFO] - ReviewService/createReview - 리뷰작성 성공, id={}", review.getId());

        return review.getId();
    }
}
