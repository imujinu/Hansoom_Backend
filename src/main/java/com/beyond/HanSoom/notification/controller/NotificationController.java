package com.beyond.HanSoom.notification.controller;

import com.beyond.HanSoom.common.dto.CommonSuccessDto;
import com.beyond.HanSoom.notification.dto.NotificationListResDto;
import com.beyond.HanSoom.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
