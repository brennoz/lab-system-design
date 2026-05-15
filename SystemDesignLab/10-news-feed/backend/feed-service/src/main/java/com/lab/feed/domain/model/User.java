package com.lab.feed.domain.model;

import java.time.Instant;
import java.util.UUID;

// Aggregate: followerCount denormalized — avoids N queries per feed read for celebrity detection
public record User(UUID id, String email, String passwordHash, int followerCount, Instant createdAt) {

    private static final int CELEBRITY_THRESHOLD = 10_000;

    public static User of(String email, String passwordHash) {
        return new User(UUID.randomUUID(), email, passwordHash, 0, Instant.now());
    }

    // Pattern: celebrity threshold — fan-out strategy switches at 10k followers
    public boolean isCelebrity() {
        return followerCount >= CELEBRITY_THRESHOLD;
    }

    public User withFollowerCount(int count) {
        return new User(id, email, passwordHash, count, createdAt);
    }
}
