package com.beyond.HanSoom.notification.dto;

import com.beyond.HanSoom.notification.domain.Notification;
import com.beyond.HanSoom.notification.domain.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationListResDto {
    private Long id;
    private Long reservationId;
    private Long hotelId;
    private LocalDateTime createdTime;
    private String title;
    private String body;
    private NotificationType type;

    public static NotificationListResDto fromEntity(Notification notification) {
        return NotificationListResDto
                .builder()
                .id(notification.getId())
                .reservationId(notification.getReservation() != null ? notification.getReservation().getId() : null)
                .hotelId(notification.getHotel() != null ? notification.getHotel().getId() : null)
                .createdTime(notification.getCreatedTime())
                .title(notification.getTitle())
                .body(notification.getBody())
                .type(notification.getType())
                .build();
    }
}
