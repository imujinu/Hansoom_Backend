package com.beyond.HanSoom.user.dto;

import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.domain.UserState;
import com.beyond.HanSoom.user.domain.UserType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateDto {
    @NotEmpty(message = "이메일이 비어있습니다.")
    private String email;
    @NotEmpty(message = "이름이 비어있습니다.")
    private String name;
    private String nickName;
    @NotEmpty(message = "비밀번호가 비어있습니다.")
    @Size(min = 8, message = "비밀번호가 8자리 미만입니다.")
    private String password;
    @NotEmpty
    private String phoneNumber;
    private UserType type;

    public User toEntity(String encodedPassword) {
        return User
                .builder()
                .email(this.email)
                .name(this.name)
                .nickName(this.nickName)
                .password(encodedPassword)
                .phoneNumber(this.phoneNumber)
                .type(this.type) // Todo - this.type이 null일 때, default값 들어가는 지 확인
                .build();
    }
}
