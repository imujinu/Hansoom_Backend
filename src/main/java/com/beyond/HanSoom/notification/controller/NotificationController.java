package com.beyond.HanSoom.notification.controller;

import com.beyond.HanSoom.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/noti")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    // 알림 등록
    // controller는 불필요

    // 알림 목록 (사용자 or host)
    // unread 상태만

}
