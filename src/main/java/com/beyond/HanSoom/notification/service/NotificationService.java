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
        // [호텔명] 새 예약 확정 🎉
        // [예약자명]님이 [체크인일 ~ 체크아웃일] 예약을 완료했습니다.

        // title
        String hotelName = reservation.getHotel().getHotelName();
        String title = "[" + hotelName + "] 새 예약 확정 🎉";
        // body
        String userName = user.getName();
        String body = "[" + userName +"]님이 [" + reservation.getCheckInDate() + " ~ " + reservation.getCheckOutDate() + "] 예약을 완료했습니다.";

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
        // [호텔명] 예약 확정 완료 ✅
        // [체크인일 ~ 체크아웃일] 숙박 예약이 정상적으로 완료되었습니다. 예약번호: [번호]

        // title
        String hotelName = reservation.getHotel().getHotelName();
        String title = "[" + hotelName + "] 예약 확정 완료 ✅";
        // body
        String userName = user.getName();
        String body = "[" + reservation.getCheckInDate() + " ~ " + reservation.getCheckOutDate() + "숙박 예약이 정상적으로 완료되었습니다.";

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
        // [호텔명] 내일 체크인 안내 🛎️
        // 내일 [체크인일] 부터 숙박이 시작됩니다. 체크인은 오후 3시 이후 가능합니다.

        // title
        String hotelName = reservation.getHotel().getHotelName();
        String title = "[" + hotelName + "] 내일 체크인 안내 🔔";
        // body
        String userName = user.getName();
        String body = "내일 [" + reservation.getCheckInDate() + "] 부터 숙박이 시작됩니다.";

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
        // [호텔명] 숙박은 어떠셨나요? ✨
        // 고객님의 소중한 리뷰가 다른 여행자들에게 큰 도움이 됩니다. 리뷰를 작성해주시면 [포인트/쿠폰]을 드려요.

        // title
        String hotelName = reservation.getHotel().getHotelName();
        String title = "[" + hotelName + "] 숙박은 어떠셨나요? ✨";
        // body
        String userName = user.getName();
        String body = "고객님의 소중한 리뷰가 다른 여행자들에게 큰 도움이 됩니다.\n리뷰 쓰러 가기 👉";

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
        // [호텔명] 호텔 등록 심사 요청 📩
        // 호스트 [호스트명]님이 호텔 등록을 요청했습니다. 검토가 필요합니다.

        // title
        String title = "[" + hotel.getHotelName() + "] 호텔 등록 심사 요청 📩";
        // body
        String hotelName = hotel.getHotelName();
        String hostName = hotel.getUser().getName();
        String body = "호스트 [" + hostName + "]님이 호텔 등록을 요청했습니다. 검토가 필요합니다.";

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

    // 알림 상태 변경
    public void updateNotificationState(Long id, NotificationState state) {
        Notification notification = notificationRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("없는 알림입니다."));
        notification.updatedState(state);
        
        log.info("[HANSOOM][INFO] - NotificationService/updateNotificationState - 알림상태 수정 성공, id={}, state={}", id, state);
    }

    // 예약 취소에 따른 모든 알림상태 취소로 변경
    public void cancelAllNotificationsByReservation(Long reservationID) {
        List<Notification> notificationList = notificationRepository.findAllByReservationId(reservationID);
        for(Notification notification : notificationList) {
            notification.updatedState(NotificationState.CANCELED);
        }
    }

}
