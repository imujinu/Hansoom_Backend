package com.beyond.HanSoom.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedirectDto {
    private String code;
    private boolean rememberMe = false;
}
