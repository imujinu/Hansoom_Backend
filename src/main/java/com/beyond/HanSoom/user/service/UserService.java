package com.beyond.HanSoom.user.service;

import com.beyond.HanSoom.common.service.LinkTicketPayload;
import com.beyond.HanSoom.common.service.LinkTicketService;
import com.beyond.HanSoom.common.service.S3Uploader;
import com.beyond.HanSoom.user.domain.SocialType;
import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.domain.UserState;
import com.beyond.HanSoom.user.dto.*;
import com.beyond.HanSoom.user.repository.UserRepository;
import com.beyond.HanSoom.common.auth.JwtTokenProvider;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final GoogleService googleService;
    private final KakaoService kakaoService;
    private final S3Uploader s3Uploader;
    private final LinkTicketService linkTicketService;


    // 회원가입
    public void save(UserCreateDto dto, MultipartFile profileImage) {
        // 검증 (중복 이메일)
        if(userRepository.findByEmail(dto.getEmail()).isPresent()) throw new IllegalArgumentException("중복되는 이메일입니다.");

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = dto.toEntity(encodedPassword);
        userRepository.save(user);

        log.info("[HANSOOM][INFO] - UserService/save - 회원가입 성공, email={}", dto.getEmail());
        
        String profileImageUrl = (profileImage != null && !profileImage.isEmpty())
                ? s3Uploader.upload(profileImage, "user")
                : null;
        user.updateProfileImage(profileImageUrl);
    }

    // 로그인
    public UserLoginResDto login(UserLoginReqDto dto) {
        // 이메일, 비밀번호 검증
        User user = userRepository.findByEmail(dto.getEmail()).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));
        boolean isValidPassword = passwordEncoder.matches(dto.getPassword(), user.getPassword());
        if(!isValidPassword) throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        if(user.getState() == UserState.WITHDRAW) throw new IllegalArgumentException("탈퇴한 회원입니다.");

        // 토큰 생성해서 반환
        String accessToken = jwtTokenProvider.createAtToken(user);
        String refreshToken = jwtTokenProvider.createRtToken(user, dto.isRememberMe());

        log.info("[HANSOOM][INFO] - UserService/login - 로그인 성공, email={}", dto.getEmail());

        return new UserLoginResDto(accessToken, refreshToken);
    }

    // 구글 로그인 (정보 없으면 회원가입까지)
    public UserLoginResDto googleLogin(RedirectDto dto) {

        // accessToken 발급
        AccessTokenDto accessTokenDto = googleService.getAccessToken(dto.getCode());
        // 사용자 정보 얻기
        GoogleProfileDto googleProfileDto = googleService.getGoogleProfile(accessTokenDto.getAccess_token());

        // 회원가입이 되어있지 않다면 회원가입
        User user = userRepository.findBySocialId(googleProfileDto.getSub()).orElse(null);
        if(user == null) {
            boolean isValid = userRepository.findByEmail(googleProfileDto.getEmail()).isPresent();
            if(isValid) {
                LinkTicketPayload payload = new LinkTicketPayload(googleProfileDto.getEmail(), googleProfileDto.getSub(), "GOOGLE");
                String ticket = linkTicketService.createTicket(payload);
                throw new EntityExistsException(ticket);
            }

            user = googleService.createOauth(googleProfileDto);
            userRepository.save(user); // 여기서 이메일 중복 오류
            log.info("[HANSOOM][INFO] - UserService/googleLogin - google 회원가입 성공, email={}", user.getEmail());
        }
        if(user.getState() == UserState.WITHDRAW) throw new IllegalArgumentException("탈퇴한 회원입니다.");

        // 토큰 생성해서 반환
        String accessToken = jwtTokenProvider.createAtToken(user);
        String refreshToken = jwtTokenProvider.createRtToken(user, dto.isRememberMe());

        log.info("[HANSOOM][INFO] - UserService/googleLogin - google 로그인 성공, email={}", user.getEmail());

        return new UserLoginResDto(accessToken, refreshToken);
    }

    // 구글 연동 및 로그인
    public UserLoginResDto googleReLogin(RedirectLinkTicketDto dto) {
        String linkTicket = dto.getLinkTicket();
        LinkTicketPayload payload = linkTicketService.consumeTicket(linkTicket);
        String email = payload.email();
        String socialId = payload.sub();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));
        if(user.getState() == UserState.WITHDRAW) throw new IllegalArgumentException("탈퇴한 회원입니다.");

        user.setSocialType(SocialType.GOOGLE);
        user.setSocialId(socialId);

        // 토큰 생성해서 반환
        String accessToken = jwtTokenProvider.createAtToken(user);
        String refreshToken = jwtTokenProvider.createRtToken(user, dto.isRememberMe());

        log.info("[HANSOOM][INFO] - UserService/googleLogin - google 연동 및 로그인 성공, email={}", user.getEmail());

        return new UserLoginResDto(accessToken, refreshToken);
    }

    // 카카오 로그인 (정보 없으면 회원가입까지)
    public UserLoginResDto kakaoLogin(RedirectDto dto) {

        // accessToken 발급
        AccessTokenDto accessTokenDto = kakaoService.getAccessToken(dto.getCode());
        // 사용자 정보 얻기
        KakaoProfileDto kakaoProfileDto = kakaoService.getKakaoProfile(accessTokenDto.getAccess_token());

        // 회원가입이 되어있지 않다면 회원가입
        User user = userRepository.findBySocialId(kakaoProfileDto.getId()).orElse(null);
        if(user == null) {
            user = kakaoService.createOauth(kakaoProfileDto);
            userRepository.save(user);
            log.info("[HANSOOM][INFO] - UserService/googleLogin - kakao 회원가입 성공, email={}", user.getEmail());
        }
        if(user.getState() == UserState.WITHDRAW) throw new IllegalArgumentException("탈퇴한 회원입니다.");

        // 토큰 생성해서 반환
        String accessToken = jwtTokenProvider.createAtToken(user);
        String refreshToken = jwtTokenProvider.createRtToken(user, dto.isRememberMe());

        log.info("[HANSOOM][INFO] - UserService/googleLogin - kakao 로그인 성공, email={}", user.getEmail());

        return new UserLoginResDto(accessToken, refreshToken);
    }

    // 로그아웃
    public String logout(String refreshToken) {
        String email = jwtTokenProvider.removeRt(refreshToken);

        log.info("[HANSOOM][INFO] - UserService/logout - 로그아웃 성공, email={}", email);

        return email;
    }

    // 토큰 재발급
    public UserLoginResDto tokenRefresh(String refreshToken) {
        User user = jwtTokenProvider.validateRt(refreshToken);
        String accessToken = jwtTokenProvider.createAtToken(user);

        log.info("[HANSOOM][INFO] - UserService/tokenRefresh - refresh token 갱신 성공, user.id={}", user.getEmail());

        return new UserLoginResDto(accessToken, null);
    }

    // 사용자 조회 (페이징, 검색 옵션) // Todo - 호텔 별 사용자 필터링
    public Page<UserListDto> findAll(Pageable pageable, UserSearchDto dto) {
        // 검색 조건
        Specification specification = UserSpecification.search(dto);

        Page<User> userPages = userRepository.findAll(specification ,pageable);

        log.info("[HANSOOM][INFO] - UserService/findAll - 사용자 리스트 조회 성공");

        return userPages.map(a -> UserListDto.fromEntity(a));
    }

    // 사용자 상세 조회 (호스트, 관리자 기준)
    public UserDetailDto findByIdForManagement(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));

        log.info("[HANSOOM][INFO] - UserService/findByIdForManagement - 회원상세 조회 성공, email={}", user.getEmail());

        return UserDetailDto.fromEntity(user);
    }

    // 사용자 상세 조회 (마이페이지)
    public UserMypageDto findByAuthenticationForSelf() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));

        log.info("[HANSOOM][INFO] - UserService/findByIdForSelf - 마이페이지 조회 성공, email={}", user.getEmail());

        return UserMypageDto.fromEntity(user);
    }

    // 사용자 정보 수정 (마이페이지)
    public Long updateUser(UserUpdateDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));
        user.updateUserInfo(dto.getName(), dto.getNickName(), dto.getPhoneNumber());


        if(dto.getProfileImage() != null && !dto.isRemoveProfileImage()) {
            String profileImageUrl = (!dto.getProfileImage().isEmpty())
                    ? s3Uploader.upload(dto.getProfileImage(), "user")
                    : null;
            user.updateProfileImage(profileImageUrl);
        } else if(dto.isRemoveProfileImage()) {
            user.updateProfileImage(null);
        }

        log.info("[HANSOOM][INFO] - UserService/updateUser - 사용자 정보 수정 성공, email={}", email);

        return user.getId();
    }

    // 회원 탈퇴 (사용자 기준)
    public Long deleteUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));
        user.setState(UserState.WITHDRAW);

        log.info("[HANSOOM][INFO] - UserService/deleteUser - 회원탈퇴 성공, email={}", email);

        return user.getId();
    }

    // 회원 탈퇴 (관리자 기준)
    public Long deleteUserByAdmin(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));
        user.setState(UserState.WITHDRAW);

        log.info("[HANSOOM][INFO] - UserService/deleteUserByAdmin - 회원탈퇴 성공, id={}", id);

        return user.getId();
    }
}
