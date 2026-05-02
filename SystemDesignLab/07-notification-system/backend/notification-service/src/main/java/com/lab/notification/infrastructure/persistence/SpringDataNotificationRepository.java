package com.lab.notification.infrastructure.persistence;

import com.lab.notification.domain.model.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataNotificationRepository extends JpaRepository<NotificationJpaEntity, UUID> {
    boolean existsByIdAndStatus(UUID id, NotificationStatus status);
}
