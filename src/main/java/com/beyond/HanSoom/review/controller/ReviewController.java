package com.beyond.HanSoom.review.controller;

import com.beyond.HanSoom.common.dto.CommonSuccessDto;
import com.beyond.HanSoom.review.dto.ReviewCreateReqDto;
import com.beyond.HanSoom.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.json.HTTP;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    // 리뷰 수정

    // 리뷰
}
