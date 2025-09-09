package com.beyond.HanSoom.user.dto;

import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.domain.UserState;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailDto {
    private String email;
    private String name;
    private String nickName;
    private String phoneNumber;
    private String profileImage;
    private UserState userState;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    static public UserDetailDto fromEntity(User user) {
        return UserDetailDto.builder()
                .email(user.getEmail())
                .name(user.getName())
                .nickName(user.getNickName())
                .phoneNumber(user.getPhoneNumber())
                .profileImage(user.getProfileImage())
                .userState(user.getState())
                .createdTime(user.getCreatedTime())
                .updatedTime(user.getUpdatedTime())
                .build();
    }
}