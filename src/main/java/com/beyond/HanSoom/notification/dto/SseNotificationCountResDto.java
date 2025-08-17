package com.beyond.HanSoom.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SseNotificationCountResDto {
    private String receiver;
    private Long notificationId;
    private int notificationCount;
}
