package com.lab.feed.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "feed_users")
public class UserJpaEntity {

    @Id
    UUID id;
    @Column(unique = true)
    String email;
    String passwordHash;
    int followerCount;
    Instant createdAt;

    protected UserJpaEntity() {}

    public UserJpaEntity(UUID id, String email, String passwordHash, int followerCount, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.followerCount = followerCount;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public int getFollowerCount() { return followerCount; }
    public Instant getCreatedAt() { return createdAt; }
}
