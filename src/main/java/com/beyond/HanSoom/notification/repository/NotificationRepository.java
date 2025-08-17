package com.beyond.HanSoom.notification.repository;

import com.beyond.HanSoom.notification.domain.Notification;
import com.beyond.HanSoom.notification.domain.NotificationState;
import com.beyond.HanSoom.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findAllByUserAndState(User user, NotificationState state);
}
