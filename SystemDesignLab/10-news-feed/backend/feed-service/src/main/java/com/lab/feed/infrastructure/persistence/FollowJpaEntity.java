package com.lab.feed.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "follows",
       uniqueConstraints = @UniqueConstraint(columnNames = {"followerId", "followeeId"}))
public class FollowJpaEntity {

    @Id
    UUID id;
    String followerId;
    String followeeId;
    Instant createdAt;

    protected FollowJpaEntity() {}

    public FollowJpaEntity(UUID id, String followerId, String followeeId, Instant createdAt) {
        this.id = id;
        this.followerId = followerId;
        this.followeeId = followeeId;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getFollowerId() { return followerId; }
    public String getFolloweeId() { return followeeId; }
    public Instant getCreatedAt() { return createdAt; }
}
