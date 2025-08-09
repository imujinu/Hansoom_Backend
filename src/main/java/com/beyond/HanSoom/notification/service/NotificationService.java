package com.beyond.HanSoom.notification.service;

import com.beyond.HanSoom.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    // 알림 등록

    // 알림 목록 (사용자 or host)
    // unread 상태만

}
