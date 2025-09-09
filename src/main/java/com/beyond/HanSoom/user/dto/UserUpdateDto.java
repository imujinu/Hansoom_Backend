package com.beyond.HanSoom.user.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDto {
    @NotEmpty(message = "이름이 비어있습니다.")
    private String name;
    private String nickName;
    @NotEmpty(message = "전화번호가 비어있습니다.")
    private String phoneNumber;
    private MultipartFile profileImage;
    private boolean removeProfileImage;
}
