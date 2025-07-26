package com.beyond.HanSoom.user.dto;

import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMypageDto {
    private String email;
    private String name;
    private String nickName;
    private String phoneNumber;
    private UserRole userRole;
    private String profileImage;
    private LocalDateTime createdTime;

    public static UserMypageDto fromEntity(User user) {
        return UserMypageDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .nickName(user.getNickName())
                .phoneNumber(user.getPhoneNumber())
                .userRole(user.getUserRole())
                .profileImage(user.getProfileImage())
                .createdTime(user.getCreatedTime())
                .build();
    }
}
