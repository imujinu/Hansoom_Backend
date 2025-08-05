package com.beyond.HanSoom.review.controller;

import com.beyond.HanSoom.common.dto.CommonSuccessDto;
import com.beyond.HanSoom.review.dto.ReviewCreateReqDto;
import com.beyond.HanSoom.review.dto.ReviewListResDto;
import com.beyond.HanSoom.review.dto.ReviewUpdateReqDto;
import com.beyond.HanSoom.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    // 리뷰작성
    @PostMapping("/create")
    public ResponseEntity<?> createReview(@ModelAttribute @Valid ReviewCreateReqDto dto) {
        Long id = reviewService.createReview(dto);
        return new ResponseEntity<>(new CommonSuccessDto(id, HttpStatus.CREATED.value(), "리뷰작성 성공"), HttpStatus.CREATED);
    }

    // 사용자가 작성한 모든 리뷰목록 (User)
    // 페이징 처리, 일단 검색은 X
    @GetMapping("/user/list")
    public ResponseEntity<?> getUserReviews(@PageableDefault(size = 5, sort = "id", direction = Sort.Direction.DESC)Pageable pageable) {
        Page<ReviewListResDto> reviewListResDtoPage = reviewService.getUserReviews(pageable);
        return new ResponseEntity<>(new CommonSuccessDto(reviewListResDtoPage, HttpStatus.OK.value(), "사용자 리뷰목록 출력 성공"), HttpStatus.OK);
    }

    // 호텔의 모든 리뷰목록 (Hotel)
    // 페이징 처리, 일단 검색은 X
//    @GetMapping("/hotel/{hotelId}/list") , getHotelReviews

    // 리뷰 상세



    // 리뷰수정
    @PutMapping("/update")
    public ResponseEntity<?> updateReview(@ModelAttribute @Valid ReviewUpdateReqDto dto) {
        reviewService.updateReview(dto);
        return new ResponseEntity<>(new CommonSuccessDto(dto.getId(), HttpStatus.OK.value(), "리뷰수정 성공"), HttpStatus.OK);
    }

    // 리뷰삭제
    @DeleteMapping("/delete/{inputId}")
    public ResponseEntity<?> deleteReview(@PathVariable Long inputId) {
        reviewService.deleteReview(inputId);
        return new ResponseEntity<>(new CommonSuccessDto(inputId, HttpStatus.OK.value(), "리뷰삭제 성공"), HttpStatus.OK);
    }
}
