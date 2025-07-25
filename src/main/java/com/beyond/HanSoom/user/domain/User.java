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
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String phoneNumber;
    @Column(nullable = false)
    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    private UserType type = UserType.USER;
    @Column(nullable = false)
    @Builder.Default
    @Enumerated(value = EnumType.STRING)
    private UserState state = UserState.NORMAL;
    private String profileImage;
    @CreationTimestamp // Todo - BaseClass로 빼기 (@MappedSuperClass)
    private LocalDateTime createdTime;
    @UpdateTimestamp
    private LocalDateTime updatedTime;

    // Todo - 필요한 경우 @OneToMany / @oneToOne 속성 등록
}
