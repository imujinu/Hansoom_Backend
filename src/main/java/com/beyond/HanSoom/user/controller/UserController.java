package com.beyond.HanSoom.user.controller;

import com.beyond.HanSoom.common.auth.CookieUtil;
import com.beyond.HanSoom.common.dto.CommonSuccessDto;
import com.beyond.HanSoom.user.dto.*;
import com.beyond.HanSoom.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Transactional
public class UserController {
    private final UserService userService;
    private final CookieUtil cookieUtil;

    @Value("${app.auth.cookie.domain:}")          private String cookieDomain;
    @Value("${app.auth.cookie.path:/auth}")       private String cookiePath;
    @Value("${app.auth.cookie.same-site:Lax}")    private String cookieSameSite;
    @Value("${app.auth.cookie.secure:false}")     private boolean cookieSecure;
    @Value("${app.auth.cookie.http-only:true}")   private boolean cookieHttpOnly;
    @Value("${jwt.refreshTokenExpiryDaysNonPersistent}")       private int refreshTokenExpiryDaysNonPersistent;
    @Value("${jwt.refreshTokenExpiryDaysPersistent}")       private int refreshTokenExpiryDaysPersistent;

    // 회원가입
    @PostMapping("/create")
    public ResponseEntity<?> createUser(@RequestPart(name = "userCreateDto") @Valid UserCreateDto dto,
                                        @RequestPart(name = "profileImage") MultipartFile profileImage) {
        userService.save(dto, profileImage);
        return new ResponseEntity<>(new CommonSuccessDto(dto.getEmail(), HttpStatus.CREATED.value(), "회원가입에 성공하였습니다."), HttpStatus.CREATED);
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid UserLoginReqDto dto) {
        UserLoginResDto userLoginResDto = userService.login(dto);
        String accessToken = userLoginResDto.getAccessToken();
        String refreshToken = userLoginResDto.getRefreshToken();

        ResponseCookie rtCookie = cookieUtil.buildRefreshTokenCookie(
                "refreshToken",
                refreshToken,
                cookieDomain,
                cookiePath,
                cookieSameSite,
                cookieSecure,
                cookieHttpOnly,
                dto.isRememberMe() ? refreshTokenExpiryDaysPersistent : refreshTokenExpiryDaysNonPersistent
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, rtCookie.toString());

        return new ResponseEntity<>(
                new CommonSuccessDto(accessToken, HttpStatus.OK.value(), "로그인 성공"),
                headers,
                HttpStatus.OK
        );
    }

    // 토큰 재발급
    @PostMapping("/auth/refresh")
    public ResponseEntity<?> tokenRefresh(@CookieValue("refreshToken") String refreshToken) {
        UserLoginResDto userLoginResDto = userService.tokenRefresh(refreshToken);
        return new ResponseEntity<>(new CommonSuccessDto(userLoginResDto, HttpStatus.OK.value(), "access token 재발급 성공"), HttpStatus.OK);
    }

    // 구글 로그인 (정보 없으면 회원가입까지)
    @PostMapping("/google/login")
    public ResponseEntity<?> googleLogin(@RequestBody RedirectDto dto) {
        UserLoginResDto userLoginResDto = userService.googleLogin(dto);

        String accessToken = userLoginResDto.getAccessToken();
        String refreshToken = userLoginResDto.getRefreshToken();

        ResponseCookie rtCookie = cookieUtil.buildRefreshTokenCookie(
                "refreshToken",
                refreshToken,
                cookieDomain,
                cookiePath,
                cookieSameSite,
                cookieSecure,
                cookieHttpOnly,
                refreshTokenExpiryDaysNonPersistent
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, rtCookie.toString());

        return new ResponseEntity<>(
                new CommonSuccessDto(accessToken, HttpStatus.OK.value(), "google 로그인 성공"),
                headers,
                HttpStatus.OK
        );
    }

    // 구글 연동 로그인
    @PostMapping("/google/reLogin")
    public ResponseEntity<?> googleReLogin(@RequestBody RedirectLinkTicketDto dto) {
        UserLoginResDto userLoginResDto = userService.googleReLogin(dto);

        String accessToken = userLoginResDto.getAccessToken();
        String refreshToken = userLoginResDto.getRefreshToken();

        ResponseCookie rtCookie = cookieUtil.buildRefreshTokenCookie(
                "refreshToken",
                refreshToken,
                cookieDomain,
                cookiePath,
                cookieSameSite,
                cookieSecure,
                cookieHttpOnly,
                refreshTokenExpiryDaysNonPersistent
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, rtCookie.toString());

        return new ResponseEntity<>(
                new CommonSuccessDto(accessToken, HttpStatus.OK.value(), "google 연동 성공"),
                headers,
                HttpStatus.OK
        );
    }

    // 카카오 로그인 (정보 없으면 회원가입까지)
    @PostMapping("/kakao/login")
    public ResponseEntity<?> kakaoLogin(@RequestBody RedirectDto dto) {
        UserLoginResDto userLoginResDto = userService.kakaoLogin(dto);

        String accessToken = userLoginResDto.getAccessToken();
        String refreshToken = userLoginResDto.getRefreshToken();

        ResponseCookie rtCookie = cookieUtil.buildRefreshTokenCookie(
                "refreshToken",
                refreshToken,
                cookieDomain,
                cookiePath,
                cookieSameSite,
                cookieSecure,
                cookieHttpOnly,
                refreshTokenExpiryDaysNonPersistent
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, rtCookie.toString());

        return new ResponseEntity<>(
                new CommonSuccessDto(accessToken, HttpStatus.OK.value(), "kakao 연동 성공"),
                headers,
                HttpStatus.OK
        );
    }

    // 로그아웃
    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(@CookieValue("refreshToken") String refreshToken) {
        String email = userService.logout(refreshToken);

        ResponseCookie rtCookie = cookieUtil.deleteCookie(
                "refreshToken",
                cookieDomain,
                cookiePath,
                cookieSameSite,
                cookieSecure,
                cookieHttpOnly
        );

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, rtCookie.toString());

        return new ResponseEntity<>(
                new CommonSuccessDto(email, HttpStatus.OK.value(), "로그아웃 성공"),
                headers,
                HttpStatus.OK
        );
    }

    // 사용자 조회 (페이징, 검색 옵션) // Todo - 호텔 별 사용자 필터링
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOST')")
    public ResponseEntity<?> getUserList(@PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)Pageable pageable,
                                         @ModelAttribute UserSearchDto dto) {
        Page<UserListDto> userPage = userService.findAll(pageable, dto);

        return new ResponseEntity<>(new CommonSuccessDto(userPage, HttpStatus.OK.value(), "사용자 리스트 조회 성공"), HttpStatus.OK);
    }

    // 사용자 상세 조회 (호스트, 관리자 기준)
    @GetMapping("/detail/{inputId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('HOST')")
    public ResponseEntity<?> getUserDetailForManagement(@PathVariable Long inputId) {
        UserDetailDto dto = userService.findByIdForManagement(inputId);
        return new ResponseEntity<>(new CommonSuccessDto(dto, HttpStatus.OK.value(), "사용자상세 조회 성공"), HttpStatus.OK);
    }

    // 사용자 상세 조회 (마이페이지)
    @GetMapping("/mypage")
    public ResponseEntity<?> getUserDetailForSelf() {
        UserMypageDto dto = userService.findByAuthenticationForSelf();
        return new ResponseEntity<>(new CommonSuccessDto(dto, HttpStatus.OK.value(), "마이페이지 조회 성공"), HttpStatus.OK);
    }

    // 사용자 정보 수정 (마이페이지)
    @PutMapping("/update")
    // Todo - 프로필 사진 수정
    public ResponseEntity<?> updateUser(@ModelAttribute @Valid UserUpdateDto dto) {
        Long userId = userService.updateUser(dto);
        return new ResponseEntity<>(new CommonSuccessDto(userId, HttpStatus.OK.value(), "사용자 정보 수정 성공"), HttpStatus.OK);
    }

    // 회원 탈퇴 (사용자 기준)
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser() {
        Long userId = userService.deleteUser();
        return new ResponseEntity<>(new CommonSuccessDto(userId, HttpStatus.OK.value(), "사용자 탈퇴 성공"), HttpStatus.OK);
    }

    // 회원 탈퇴 (관리자 기준)
    @DeleteMapping("/admin/delete/{inputId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long inputId) {
        Long userId = userService.deleteUserByAdmin(inputId);
        return new ResponseEntity<>(new CommonSuccessDto(userId, HttpStatus.OK.value(), "사용자 탈퇴 성공"), HttpStatus.OK);
    }

}
