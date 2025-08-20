package com.beyond.HanSoom.notification.controller;

import com.beyond.HanSoom.common.dto.CommonSuccessDto;
import com.beyond.HanSoom.notification.domain.NotificationState;
import com.beyond.HanSoom.notification.dto.NotificationListResDto;
import com.beyond.HanSoom.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/noti")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    // 알림 등록
    // controller는 불필요

    // 알림 목록 (UNREAD)
    // Todo - Paging 처리
    @GetMapping("/list")
    public ResponseEntity<?> getNotificationList() {
        List<NotificationListResDto> notificationListResDtoList = notificationService.getNotificationList();
        return new ResponseEntity<>(new CommonSuccessDto(notificationListResDtoList, HttpStatus.OK.value(), "알림목록 조회 성공"), HttpStatus.OK);
    }

    // 알림 읽음상태 변경
    @PatchMapping("/updateState/{notiId}")
    public ResponseEntity<?> updateNotificationState(@PathVariable Long notiId) {
        notificationService.updateNotificationState(notiId, NotificationState.READ);
        return new ResponseEntity<>(new CommonSuccessDto(notiId, HttpStatus.OK.value(), "알림상태 읽음으로 수정 성공"), HttpStatus.OK);
    }
}
