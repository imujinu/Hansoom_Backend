package com.beyond.HanSoom.review.controller;

import com.beyond.HanSoom.common.dto.CommonSuccessDto;
import com.beyond.HanSoom.review.dto.ReviewCreateReqDto;
import com.beyond.HanSoom.review.dto.ReviewUpdateReqDto;
import com.beyond.HanSoom.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.json.HTTP;
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

    // 리뷰수정
    @PutMapping("/update")
    public ResponseEntity<?> updateReview(@ModelAttribute @Valid ReviewUpdateReqDto dto) {
        reviewService.updateReview(dto);
        return new ResponseEntity<>(new CommonSuccessDto(dto.getId(), HttpStatus.OK.value(), "리뷰수정 성공"), HttpStatus.OK);
    }

    // 리뷰삭제
}
