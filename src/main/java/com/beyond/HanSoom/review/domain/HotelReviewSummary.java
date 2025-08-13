package com.beyond.HanSoom.review.domain;

import com.beyond.HanSoom.hotel.domain.Hotel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class HotelReviewSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    @Builder.Default
    private Long ratingSum = 0L;
    @Column(nullable = false)
    @Builder.Default
    private int ratingCount = 0;

    @JoinColumn(name = "hotel_id")
    @OneToOne(fetch = FetchType.LAZY)
    @Setter
    private Hotel hotel;
}
