package com.beyond.HanSoom.user.service;

import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.dto.UserCreateDto;
import com.beyond.HanSoom.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;


    // 회원가입
    public void save(UserCreateDto dto, MultipartFile profileImage) {
        // 검증 (중복 이메일)
        if(userRepository.findByEmail(dto.getEmail()).isPresent()) throw new IllegalArgumentException("중복되는 이메일입니다.");

        // Todo - 비밀번호 암호화

        User user = dto.toEntity(dto.getPassword()); // Todo - EncodedPassword 넘겨주기
        userRepository.save(user);
    }

}
