package com.beyond.HanSoom.review.domain;

import com.beyond.HanSoom.hotel.domain.Hotel;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
    private BigDecimal ratingSum = BigDecimal.ZERO;
    @Column(nullable = false)
    @Builder.Default
    private int ratingCount = 0;

    @JoinColumn(name = "hotel_id")
    @OneToOne(fetch = FetchType.LAZY)
    @Setter
    private Hotel hotel;

    public void addReviewRating(BigDecimal rating) {
         ratingSum = ratingSum.add(rating);
         ratingCount++;
    }
    public BigDecimal getAverage() {
        if (ratingCount == 0) {
            return BigDecimal.ZERO;
        }
        return ratingSum.divide(
                BigDecimal.valueOf(ratingCount),
                1,
                RoundingMode.HALF_UP
        );
    }
}
