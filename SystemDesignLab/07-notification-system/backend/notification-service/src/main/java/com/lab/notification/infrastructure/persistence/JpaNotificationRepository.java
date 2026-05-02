package com.lab.notification.infrastructure.persistence;

import com.lab.notification.domain.model.Notification;
import com.lab.notification.domain.model.NotificationStatus;
import com.lab.notification.domain.port.NotificationRepository;

import java.util.UUID;

// Pattern: Repository adapter — translates domain Notification to JPA entity
public class JpaNotificationRepository implements NotificationRepository {

    private final SpringDataNotificationRepository delegate;

    public JpaNotificationRepository(SpringDataNotificationRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public void save(Notification n) {
        delegate.save(new NotificationJpaEntity(
                n.id(), n.type(), n.recipientId(), n.channel(),
                n.priority(), n.payload(), n.status(), n.createdAt()));
    }

    @Override
    public void updateStatus(UUID notificationId, NotificationStatus status) {
        delegate.findById(notificationId).ifPresent(e -> {
            e.setStatus(status);
            delegate.save(e);
        });
    }

    @Override
    public boolean existsSent(UUID notificationId) {
        return delegate.existsByIdAndStatus(notificationId, NotificationStatus.SENT);
    }
}
