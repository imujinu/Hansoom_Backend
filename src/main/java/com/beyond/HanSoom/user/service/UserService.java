package com.beyond.HanSoom.user.service;

import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.dto.UserCreateDto;
import com.beyond.HanSoom.user.dto.UserLoginDto;
import com.beyond.HanSoom.user.repository.UserRepository;
import com.beyond.HanSoom.user.security.JwtTokenProvider;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

}
