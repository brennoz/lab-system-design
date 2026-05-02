package com.lab.notification.infrastructure.persistence;

import com.lab.notification.domain.model.Channel;
import com.lab.notification.domain.model.NotificationStatus;
import com.lab.notification.domain.model.Priority;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

// Pattern: JPA Entity — maps Notification aggregate to notifications table
@Entity
@Table(name = "notifications")
public class NotificationJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String recipientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Channel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Column(columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    protected NotificationJpaEntity() {}

    public NotificationJpaEntity(UUID id, String type, String recipientId, Channel channel,
                                  Priority priority, String payload, NotificationStatus status, Instant createdAt) {
        this.id = id;
        this.type = type;
        this.recipientId = recipientId;
        this.channel = channel;
        this.priority = priority;
        this.payload = payload;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }
}
