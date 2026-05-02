package com.lab.notification.infrastructure.inapp;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

// Pattern: JPA entity — maps in_app_notifications table; immutable after insert
@Entity
@Table(name = "in_app_notifications")
public class InAppNotificationJpaEntity {

    @Id
    private UUID id;

    @Column(name = "recipient_id", nullable = false)
    private String recipientId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected InAppNotificationJpaEntity() {}

    public InAppNotificationJpaEntity(UUID id, String recipientId, String payload, Instant createdAt) {
        this.id = id;
        this.recipientId = recipientId;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getRecipientId() { return recipientId; }
    public String getPayload() { return payload; }
    public Instant getCreatedAt() { return createdAt; }
}
