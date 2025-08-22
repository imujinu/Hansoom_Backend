package com.beyond.HanSoom.user.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false) // Todo - unique로 저장 막힐 때의 예외처리
    private String email;
    @Column(nullable = false)
    private String name;
    private String nickName;
    private String password;
    private String phoneNumber;
    @Column(nullable = false)
    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    private UserRole userRole = UserRole.USER;
    @Column(nullable = false)
    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    @Setter
    private UserState state = UserState.NORMAL;
    private String profileImage;
    @Enumerated(value = EnumType.STRING)
    @Setter
    private SocialType socialType;
    @Setter
    private String socialId;
    @CreationTimestamp // Todo - BaseClass로 빼기 (@MappedSuperClass)
    private LocalDateTime createdTime;
    @UpdateTimestamp
    private LocalDateTime updatedTime;

    // Todo - 필요한 경우 @OneToMany / @oneToOne 속성 등록

    // 사용자 정보 수정 (마이페이지)
    public void updateUserInfo(String name, String nickName, String phoneNumber) {
        this.name = name;
        this.nickName = nickName;
        this.phoneNumber = phoneNumber;
    }

    public void updateProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
