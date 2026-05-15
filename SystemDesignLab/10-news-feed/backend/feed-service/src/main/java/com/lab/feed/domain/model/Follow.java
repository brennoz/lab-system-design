package com.lab.feed.domain.model;

import java.time.Instant;
import java.util.UUID;

// Value object: directed edge followerId → followeeId in the social graph
public record Follow(UUID id, String followerId, String followeeId, Instant createdAt) {

    public static Follow of(String followerId, String followeeId) {
        return new Follow(UUID.randomUUID(), followerId, followeeId, Instant.now());
    }
}
