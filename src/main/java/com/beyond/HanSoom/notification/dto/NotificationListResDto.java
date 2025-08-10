package com.beyond.HanSoom.notification.dto;

import com.beyond.HanSoom.notification.domain.Notification;
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
    private LocalDateTime createdTime;
    private String title;
    private String body;

    public static NotificationListResDto fromEntity(Notification notification) {
        return NotificationListResDto
                .builder()
                .createdTime(notification.getCreatedTime())
                .title(notification.getTitle())
                .body(notification.getBody())
                .build();
    }
}
