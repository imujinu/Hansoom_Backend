package com.beyond.HanSoom.user.dto;

import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.domain.UserState;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDetailDto {
    private Long id;
    private String email;
    private String name;
    private String nickName;
    private String phoneNumber;
    private String profileImage;
    private UserState userState;

    static public UserDetailDto fromEntity(User user) {
        return UserDetailDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .nickName(user.getNickName())
                .phoneNumber(user.getPhoneNumber())
                .profileImage(user.getProfileImage())
                .userState(user.getState())
                .build();
    }
}