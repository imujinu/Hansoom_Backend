package com.beyond.HanSoom.notification.service;

import com.beyond.HanSoom.notification.domain.Notification;
import com.beyond.HanSoom.notification.domain.NotificationState;
import com.beyond.HanSoom.notification.domain.NotificationType;
import com.beyond.HanSoom.notification.dto.NotificationListResDto;
import com.beyond.HanSoom.notification.repository.NotificationRepository;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.user.domain.User;
import com.beyond.HanSoom.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // 알림 등록
    // NEW_BOOKING_FOR_HOST
    public Long createNotiNewBookingForHost(User user, User host, Reservation reservation) {
        // title
        String hotelName = reservation.getHotel().getHotelName();
        String title = "[" + hotelName + "] 새로운 예약이 들어왔습니다.";
        // body
        String userName = user.getName();
        String body = "예약자: " + userName +", 체크인: " + reservation.getCheckInDate() + ", 체크아웃: " + reservation.getCheckOutDate();

        Notification notification = Notification.builder()
                .title(title)
                .body(body)
                .type(NotificationType.NEW_BOOKING_FOR_HOST)
                .user(host)
                .reservation(reservation)
                .build();

        notificationRepository.save(notification);

        log.info("[HANSOOM][INFO] - NotificationService/createNotiNewBookingForHost - 알림 생성 성공, id={}", notification.getId());

        return notification.getId();
    }

    // 알림 목록 (UNREAD)
    public List<NotificationListResDto> getNotificationList() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));
        List<NotificationListResDto> notificationListResDtoList
                = notificationRepository.findAllByUserAndState(user, NotificationState.UNREAD).stream().map(a -> NotificationListResDto.fromEntity(a)).toList();

        log.info("[HANSOOM][INFO] - NotificationService/getNotificationList - 알림목록 조회 성공");

        return notificationListResDtoList;
    }

}
