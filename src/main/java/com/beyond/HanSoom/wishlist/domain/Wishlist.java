package com.beyond.HanSoom.wishlist.domain;

import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.user.domain.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

// [Spring] 찜 기능 구현하기
// 참고 블로그 링크 : https://velog.io/@mk020/Spring-%EC%B0%9C-%EA%B8%B0%EB%8A%A5-%EA%B5%AC%ED%98%84%ED%95%98%EA%B8%B0

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Builder
public class Wishlist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // 찜 id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id")
    private User user; // 유저 id

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="hotel_id")
    private Hotel hotel; // 호텔 id

    @CreationTimestamp
    private LocalDateTime createdTime; // 찜 생성시간
}
