package com.beyond.HanSoom.reply.service;

import com.beyond.HanSoom.reply.domain.Reply;
import com.beyond.HanSoom.reply.dto.ReplyCreateReqDto;
import com.beyond.HanSoom.reply.dto.ReplyUpdateReqDto;
import com.beyond.HanSoom.reply.repository.ReplyRepository;
import com.beyond.HanSoom.review.domain.Review;
import com.beyond.HanSoom.review.repository.ReviewRepository;
import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReplyService {
    private final ReplyRepository replyRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    // 답글 작성
    public Long createReply(ReplyCreateReqDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("없는 사용자입니다."));
        Review review = reviewRepository.findById(dto.getReviewId()).orElseThrow(() -> new EntityNotFoundException("없는 리뷰입니다."));
        Reply reply = dto.toEntity(user, review);

        replyRepository.save(reply);

        log.info("[HANSOOM][INFO] - ReplyService/createReply - 답글작성 성공, id={}", reply.getId());

        return reply.getId();
    }

    // 답글 수정
    public void updateReply(ReplyUpdateReqDto dto) {
        Reply reply = replyRepository.findById(dto.getReplyId()).orElseThrow(() -> new EntityNotFoundException("없는 답글입니다."));
        reply.updateContents(dto.getContents());
        
        log.info("[HANSOOM][INFO] - ReplyService/updateReply - 답글수정 성공, id={}", dto.getReplyId());
    }

    // 답글 삭제
    public void deleteReply(Long id) {
        Reply reply = replyRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("없는 답글입니다."));
        reply.deleteReply();

        log.info("[HANSOOM][INFO] - ReplyService/deleteReply - 답글삭제 성공, id={}", id);
    }

    public Long getRepliesCount(Long id) {
        return replyRepository.countByHotelId(id);
    }

}
