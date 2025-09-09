package com.beyond.HanSoom.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLoginReqDto {
    @NotEmpty(message = "이메일이 비어있습니다.")
    private String email;
    @NotEmpty(message = "비밀번호가 비어있습니다.")
    @Size(min = 8, message = "비밀번호가 8자리 미만입니다.")
    private String password;
    private boolean rememberMe = false;
}
