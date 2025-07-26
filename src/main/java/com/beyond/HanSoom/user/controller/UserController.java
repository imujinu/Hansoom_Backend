package com.beyond.HanSoom.user.controller;

import com.beyond.HanSoom.common.CommonSuccessDto;
import com.beyond.HanSoom.user.dto.UserCreateDto;
import com.beyond.HanSoom.user.dto.UserDetailDto;
import com.beyond.HanSoom.user.dto.UserLoginDto;
import com.beyond.HanSoom.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    // 회원가입
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestPart(name = "userCreateDto") @Valid UserCreateDto dto,
                                        @RequestPart(name = "profileImage") MultipartFile profileImage) {
        userService.save(dto, profileImage);
        return new ResponseEntity<>(new CommonSuccessDto(dto.getEmail(), HttpStatus.CREATED.value(), "회원가입에 성공하였습니다."), HttpStatus.CREATED);
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDto dto) {
        String token = userService.login(dto);
        return new ResponseEntity<>(new CommonSuccessDto(token, HttpStatus.OK.value(), "로그인 성공"), HttpStatus.OK);
    }

    // 로그아웃

    // 사용자 조회 (페이징, 검색 옵션) // Todo - 호텔 별 사용자 필터링

    // 사용자 상세 조회 (호스트, 관리자 기준)
    @GetMapping("/detail/{inputId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOST')")
    public ResponseEntity<?> getUserDetailForManagement(@PathVariable Long inputId) {
        UserDetailDto dto = userService.findById(inputId);
        return new ResponseEntity<>(new CommonSuccessDto(dto, HttpStatus.OK.value(), "사용자상세 조회 성공"), HttpStatus.OK);
    }

    // 사용자 상세 조회 (마이페이지)

    // 사용자 정보 수정 (마이페이지)

    // 회원 탈퇴


}
