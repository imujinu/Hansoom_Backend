package com.beyond.HanSoom.notification.service;

import com.beyond.HanSoom.hotel.domain.Hotel;
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

import static java.time.LocalDateTime.now;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // 알림 등록
    // NEW_BOOKING_FOR_HOST
    public Long createNotiNewBookingForHost(User user, Reservation reservation) {
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
                .user(reservation.getHotel().getUser())
                .reservation(reservation)
                .showAtTime(now())
                .expiresAtTime(now().plusDays(30))
                .build();

        notificationRepository.save(notification);

        log.info("[HANSOOM][INFO] - NotificationService/createNotiNewBookingForHost - 알림 생성 성공, id={}", notification.getId());

        return notification.getId();
    }

    // 알림 등록
    // BOOKING_CONFIRMED
    public Long createNotiBookingConfirmed(User user, Reservation reservation) {
        // title
        String hotelName = reservation.getHotel().getHotelName();
        String title = "[" + hotelName + "] 예약이 완료되었습니다.";
        // body
        String userName = user.getName();
        String body = "예약자: " + userName +", 체크인: " + reservation.getCheckInDate() + ", 체크아웃: " + reservation.getCheckOutDate();

        Notification notification = Notification.builder()
                .title(title)
                .body(body)
                .type(NotificationType.BOOKING_CONFIRMED)
                .user(user)
                .reservation(reservation)
                .showAtTime(now())
                .expiresAtTime(now().plusDays(30))
                .build();

        notificationRepository.save(notification);

        log.info("[HANSOOM][INFO] - NotificationService/createNotiBookingConfirmed - 알림 생성 성공, id={}", notification.getId());

        return notification.getId();
    }

    // 알림 등록
    // STAY_REMINDER_D1
    public Long createNotiStayReminderD1(User user, Reservation reservation) {
        // title
        String hotelName = reservation.getHotel().getHotelName();
        String title = "[" + hotelName + "] 입실이 하루남았습니다.";
        // body
        String userName = user.getName();
        String body = "예약자: " + userName +", 체크인: " + reservation.getCheckInDate() + ", 체크아웃: " + reservation.getCheckOutDate();

        Notification notification = Notification.builder()
                .title(title)
                .body(body)
                .type(NotificationType.STAY_REMINDER_D1)
                .user(user)
                .reservation(reservation)
                .showAtTime(reservation.getCheckInDate()
                        .minusDays(1)
                        .atStartOfDay())
                .expiresAtTime(reservation.getCheckInDate()
                        .atTime(12, 0))
                .build();

        notificationRepository.save(notification);

        log.info("[HANSOOM][INFO] - NotificationService/createNotiStayReminderD1 - 알림 생성 성공, id={}", notification.getId());

        return notification.getId();
    }

    // 알림 등록
    // REVIEW_REQUEST
    public Long createNotiReviewRequest(User user, Reservation reservation) {
        // title
        String hotelName = reservation.getHotel().getHotelName();
        String title = "[" + hotelName + "] 만족스러우셨다면 리뷰를 남겨주세요!";
        // body
        String userName = user.getName();
        String body = "리뷰 남기러가기 👉";

        Notification notification = Notification.builder()
                .title(title)
                .body(body)
                .type(NotificationType.REVIEW_REQUEST)
                .user(user)
                .reservation(reservation)
                .showAtTime(reservation.getCheckOutDate()
                        .plusDays(1)
                        .atStartOfDay())
                .expiresAtTime(reservation.getCheckOutDate()
                        .plusDays(30)
                        .atStartOfDay())
                .build();

        notificationRepository.save(notification);

        log.info("[HANSOOM][INFO] - NotificationService/createNotiReviewRequest - 알림 생성 성공, id={}", notification.getId());

        return notification.getId();
    }

    // 알림 등록
    // NEW_HOTEL_SUBMITTED
    public Long createNotiNewHotelSubmitted(User user, Hotel hotel) {
        // title
        String title = "새로운 호텔등록 요청이 들어왔습니다.";
        // body
        String hotelName = hotel.getHotelName();
        String hostName = hotel.getUser().getName();
        String body = "호텔명: " + hotelName + ", 호스트명: " + hostName;

        Notification notification = Notification.builder()
                .title(title)
                .body(body)
                .type(NotificationType.NEW_HOTEL_SUBMITTED)
                .user(user)
                .hotel(hotel)
                .showAtTime(now())
                .expiresAtTime(now().plusDays(30))
                .build();

        notificationRepository.save(notification);

        log.info("[HANSOOM][INFO] - NotificationService/createNotiNewHotelSubmitted - 알림 생성 성공, id={}", notification.getId());

        return notification.getId();
    }

    // 알림 목록 (UNREAD)
    public List<NotificationListResDto> getNotificationList() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("없는 회원입니다."));
//        List<NotificationListResDto> notificationListResDtoList
//                = notificationRepository.findAllByUserAndState(user, NotificationState.UNREAD).stream().map(a -> NotificationListResDto.fromEntity(a)).toList();

        List<NotificationListResDto> notificationListResDtoList
                = notificationRepository.findVisibleByUserAndStateAt(user, NotificationState.UNREAD, now()).stream().map(NotificationListResDto::fromEntity).toList();

        log.info("[HANSOOM][INFO] - NotificationService/getNotificationList - 알림목록 조회 성공");

        return notificationListResDtoList;
    }

    // 알림 읽음상태 변경
    public void updateNotificationState(Long id) {
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("없는 알림입니다."));
        notification.updatedReadState();
        
        log.info("[HANSOOM][INFO] - NotificationService/updateNotificationState - 알림상태 읽음으로 수정 성공, id={}", id);
    }

}
