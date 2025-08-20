package com.beyond.HanSoom.notification.repository;

import com.beyond.HanSoom.notification.domain.Notification;
import com.beyond.HanSoom.notification.domain.NotificationState;
import com.beyond.HanSoom.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("""
           SELECT n
           FROM Notification n
           WHERE n.user = :user
             AND n.state = :state
             AND n.showAtTime <= :now
             AND n.expiresAtTime >= :now
           ORDER BY n.showAtTime DESC
           """)
    List<Notification> findVisibleByUserAndStateAt(
            @Param("user") User user,
            @Param("state") NotificationState state,
            @Param("now") LocalDateTime now
    );

    List<Notification> findAllByReservationId(Long reservationId);
}
