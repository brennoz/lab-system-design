package com.lab.flight.domain.model;

import java.time.Instant;
import java.util.UUID;

// Value Object — Outbox pattern: persisted atomically with aggregate, published async by poller
public record OutboxEvent(
        UUID id,
        UUID aggregateId,
        String eventType,
        String payload,
        boolean published,
        Instant createdAt
) {
    public static OutboxEvent unpublished(UUID aggregateId, String eventType, String payload) {
        return new OutboxEvent(UUID.randomUUID(), aggregateId, eventType, payload, false, Instant.now());
    }
}
