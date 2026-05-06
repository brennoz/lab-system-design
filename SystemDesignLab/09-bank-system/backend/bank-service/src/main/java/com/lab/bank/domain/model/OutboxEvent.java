package com.lab.bank.domain.model;

import java.time.Instant;
import java.util.UUID;

// Pattern: Outbox — persisted with the aggregate in same TX; poller delivers to Kafka independently
public record OutboxEvent(UUID id, UUID aggregateId, String eventType, String payload,
                          boolean published, Instant createdAt) {

    public static OutboxEvent unpublished(UUID aggregateId, String eventType, String payload) {
        return new OutboxEvent(UUID.randomUUID(), aggregateId, eventType, payload, false, Instant.now());
    }
}
