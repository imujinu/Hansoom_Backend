package com.beyond.HanSoom.reply.controller;

import com.beyond.HanSoom.common.dto.CommonSuccessDto;
import com.beyond.HanSoom.reply.dto.ReplyCreateReqDto;
import com.beyond.HanSoom.reply.dto.ReplyUpdateReqDto;
import com.beyond.HanSoom.reply.service.ReplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reply")
@RequiredArgsConstructor
public class ReplyController {
    private final ReplyService replyService;

    // 답글 작성
    @PostMapping("/create")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> createReply(@RequestBody @Valid ReplyCreateReqDto dto) {
        Long id = replyService.createReply(dto);
        return new ResponseEntity<>(new CommonSuccessDto(id, HttpStatus.CREATED.value(), "답글작성 성공"), HttpStatus.CREATED);
    }

    // 답글 수정
    @PutMapping("/update/{replyId}")
    @PreAuthorize("hasRole('HOST')")
    public ResponseEntity<?> updateReply(@PathVariable Long replyId, @RequestBody ReplyUpdateReqDto dto) {
        replyService.updateReply(replyId, dto);
        return new ResponseEntity<>(new CommonSuccessDto(replyId, HttpStatus.OK.value(), "답글수정 성공"), HttpStatus.OK);
    }

    // 답글 삭제
    @DeleteMapping("/delete/{replyId}")
    @PreAuthorize("hasRole('HOST') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteReply(@PathVariable Long replyId) {
        replyService.deleteReply(replyId);
        return new ResponseEntity<>(new CommonSuccessDto(replyId, HttpStatus.OK.value(), "답글삭제 성공"), HttpStatus.OK);
    }

}
