package com.lab.notification.domain.model;

// Pattern: Value Object — per-recipient, per-channel opt-out preference
public record RecipientPreference(
        String recipientId,
        Channel channel,
        boolean optedOut
) {}
