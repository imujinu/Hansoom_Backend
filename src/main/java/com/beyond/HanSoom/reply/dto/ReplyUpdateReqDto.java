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
public class ReplyUpdateReqDto {
    private Long replyId;
    private String contents;
}
