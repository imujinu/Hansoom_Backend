package com.beyond.HanSoom.user.service;

import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.domain.UserState;
import com.beyond.HanSoom.user.dto.*;
import com.beyond.HanSoom.user.repository.UserRepository;
import com.beyond.HanSoom.user.security.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;


    // 회원가입
    public void save(UserCreateDto dto, MultipartFile profileImage) {
        // 검증 (중복 이메일)
        if(userRepository.findByEmail(dto.getEmail()).isPresent()) throw new IllegalArgumentException("중복되는 이메일입니다.");

        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        User user = dto.toEntity(encodedPassword);
        userRepository.save(user);

        log.info("[HANSOOM][INFO] - UserService/save - 회원가입 성공, email={}", dto.getEmail());

        // Todo - 프로필 사진 저장 구현
    }

    // 로그인
    public String login(UserLoginDto dto) {
        // 이메일, 비밀번호 검증
        User user = userRepository.findByEmail(dto.getEmail()).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));
        boolean isValidPassword = passwordEncoder.matches(dto.getPassword(), user.getPassword());
        if(!isValidPassword) throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");

        // 토큰 생성해서 반환
        String token = jwtTokenProvider.createAtToken(user);

        log.info("[HANSOOM][INFO] - UserService/login - 로그인 성공, email={}", dto.getEmail());

        return token;
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

    // 회원 탈퇴 (사용자 기준)
    public Long deleteUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));
        user.setState(UserState.WITHDRAW);
        return user.getId();
    }
}
