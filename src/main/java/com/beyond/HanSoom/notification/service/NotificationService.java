package com.beyond.HanSoom.notification.service;

import com.beyond.HanSoom.notification.domain.Notification;
import com.beyond.HanSoom.notification.domain.NotificationType;
import com.beyond.HanSoom.notification.repository.NotificationRepository;
import com.beyond.HanSoom.reservation.domain.Reservation;
import com.beyond.HanSoom.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

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
        return notification.getId();
    }

    // 알림 목록 (사용자 or host)
    // unread 상태만

}
