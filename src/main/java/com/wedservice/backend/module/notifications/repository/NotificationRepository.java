package com.wedservice.backend.module.notifications.repository;

import com.wedservice.backend.module.notifications.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
            select n from Notification n
            where ((n.userId = :userId) or n.isBroadcast = true)
              and ((n.sentAt is not null and n.sentAt <= :now)
                or (n.sentAt is null and (n.scheduledAt is null or n.scheduledAt <= :now)))
            order by n.createdAt desc
            """)
    List<Notification> findVisibleForUser(UUID userId, LocalDateTime now);

    @Query("""
            select n from Notification n
            where n.id = :id
              and ((n.userId = :userId) or n.isBroadcast = true)
              and ((n.sentAt is not null and n.sentAt <= :now)
                or (n.sentAt is null and (n.scheduledAt is null or n.scheduledAt <= :now)))
            """)
    Optional<Notification> findVisibleForUserById(UUID userId, Long id, LocalDateTime now);
}
