package com.lab.notification.domain.port;

import com.lab.notification.domain.model.Notification;
import com.lab.notification.domain.model.NotificationStatus;

import java.util.UUID;

// Pattern: Repository port — persists notifications; idempotency check via existsSent
public interface NotificationRepository {
    void save(Notification notification);
    void updateStatus(UUID notificationId, NotificationStatus status);
    boolean existsSent(UUID notificationId);
}
