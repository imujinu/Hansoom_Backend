package com.beyond.HanSoom.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenDto {
    @NotEmpty(message = "토큰 값이 비어있습니다.")
    private String refreshToken;
}
