package com.beyond.HanSoom.common.service;

import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.domain.UserRole;
import com.beyond.HanSoom.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// 관리자 초기화
@Component
@RequiredArgsConstructor
public class InitialDataLoader implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if(userRepository.findByEmail("admin@naver.com").isPresent()) {
            return;
        }
        User user = User.builder()
                .email("admin@naver.com")
                .name("admin")
                .userRole(UserRole.ADMIN)
                .password(passwordEncoder.encode("admin1234"))
                .phoneNumber("01012341234")
                .build();
        userRepository.save(user);
    }
}
