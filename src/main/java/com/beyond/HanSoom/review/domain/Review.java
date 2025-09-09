package com.beyond.HanSoom.review.domain;

import com.beyond.HanSoom.common.domain.BaseTimeEntity;
import com.beyond.HanSoom.hotel.domain.Hotel;
import com.beyond.HanSoom.reply.domain.Reply;
import com.beyond.HanSoom.reply.domain.ReplyState;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.review.dto.ReviewImageResDto;
import com.beyond.HanSoom.reviewImage.domain.ReviewImage;
import com.beyond.HanSoom.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Review extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(precision = 3, scale = 2, nullable = false)
    private BigDecimal rating;
    @Column(length = 1000)
    private String contents;
    @Builder.Default
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReviewState state = ReviewState.NORMAL;

    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private User user;
    @JoinColumn(name = "hotel_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Hotel hotel;
    @JoinColumn(name = "reservation_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Reservation reservation;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewImage> reviewImageList = new ArrayList<>();
    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Reply> replyList = new ArrayList<>();

    public void updateReview(BigDecimal rating, String contents) {
        this.rating = rating;
        this.contents = contents;
    }
    public void deleteReview() {
        this.state = ReviewState.REMOVE;
    }
    public List<ReviewImageResDto> getReviewImageDtoList() {
        return reviewImageList.stream().map(a -> ReviewImageResDto.fromEntity(a)).toList();
    }
    public Reply getReply() {
        Reply reply = replyList.stream().filter(a -> a.getState() == ReplyState.NORMAL).findFirst().orElse(null);
        return reply;
    }
}
