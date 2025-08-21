package com.beyond.HanSoom.reply.dto;

import com.beyond.HanSoom.reply.domain.Reply;
import com.beyond.HanSoom.review.domain.Review;
import com.beyond.HanSoom.user.domain.User;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReplyCreateReqDto {
    @NotNull(message = "리뷰 ID가 없습니다.")
    private Long reviewId;
    private String contents;

    public Reply toEntity(User user, Review review) {
        return Reply.builder()
                .contents(this.contents)
                .user(user)
                .review(review)
                .build();
    }
}
