package com.lab.flight.infrastructure.outbox;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

// Pattern: Outbox entity — published flag polled by OutboxPoller; payload stored as TEXT (JSON)
@Entity
@Table(name = "outbox_events")
public class OutboxJpaEntity {

    @Id
    private UUID id;
    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;
    @Column(name = "event_type", nullable = false)
    private String eventType;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;
    @Column(nullable = false)
    private boolean published;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OutboxJpaEntity() {}

    public OutboxJpaEntity(UUID id, UUID aggregateId, String eventType,
                           String payload, boolean published, Instant createdAt) {
        this.id = id; this.aggregateId = aggregateId; this.eventType = eventType;
        this.payload = payload; this.published = published; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public boolean isPublished() { return published; }
    public void setPublished(boolean published) { this.published = published; }
    public Instant getCreatedAt() { return createdAt; }
}
