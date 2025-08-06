package com.beyond.HanSoom.reply.controller;

import com.beyond.HanSoom.common.dto.CommonSuccessDto;
import com.beyond.HanSoom.reply.dto.ReplyCreateReqDto;
import com.beyond.HanSoom.reply.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reply")
@RequiredArgsConstructor
public class ReplyController {
    private final ReplyService replyService;

    // 답글 작성
    @PostMapping("/create")
    public ResponseEntity<?> createReply(@RequestBody ReplyCreateReqDto dto) {
        Long id = replyService.createReply(dto);
        return new ResponseEntity<>(new CommonSuccessDto(id, HttpStatus.CREATED.value(), "답글작성 성공"), HttpStatus.CREATED);
    }

    // 답글 수정

    // 답글 삭제

}
