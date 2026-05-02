package com.lab.notification.domain.model;

import java.time.Instant;
import java.util.UUID;

// Pattern: Aggregate — immutable snapshot of one notification per recipient
public record Notification(
        UUID id,
        String type,
        String recipientId,
        Channel channel,
        Priority priority,
        String payload,
        NotificationStatus status,
        Instant createdAt
) {
    public static Notification pending(String type, String recipientId,
                                       Channel channel, Priority priority, String payload) {
        return new Notification(UUID.randomUUID(), type, recipientId,
                channel, priority, payload, NotificationStatus.PENDING, Instant.now());
    }

    // Use when task UUID is already allocated — keeps notification ID and task ID in sync for idempotency
    public static Notification pendingWithId(UUID id, String type, String recipientId,
                                             Channel channel, Priority priority, String payload) {
        return new Notification(id, type, recipientId,
                channel, priority, payload, NotificationStatus.PENDING, Instant.now());
    }

    public Notification withStatus(NotificationStatus newStatus) {
        return new Notification(id, type, recipientId, channel, priority, payload, newStatus, createdAt);
    }
}
