package com.beyond.HanSoom.user.dto;

import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.domain.UserRole;
import com.beyond.HanSoom.user.domain.UserState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserListDto {
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private UserRole userRole;
    private UserState userState;
    private LocalDateTime createdTime;

    public static UserListDto fromEntity(User user) {
        return builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .userRole(user.getUserRole())
                .userState(user.getState())
                .createdTime(user.getCreatedTime())
                .build();
    }
}
