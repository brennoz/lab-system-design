package com.lab.notification.domain.model;

import java.util.UUID;

// Pattern: Value Object — one delivery unit per recipient; UUID enables idempotency check
public record NotificationTask(
        UUID notificationId,
        String recipientId,
        Channel channel,
        Priority priority,
        String payload
) {}
