package com.lab.feed.domain.model;

import java.time.Instant;
import java.util.UUID;

// Aggregate: immutable post owned by an author; likeCount denormalized for O(1) ranking score
public record Post(UUID id, String authorId, String content, int likeCount, Instant createdAt) {

    public static Post of(String authorId, String content) {
        return new Post(UUID.randomUUID(), authorId, content, 0, Instant.now());
    }

    public Post withLikeCount(int likeCount) {
        return new Post(id, authorId, content, likeCount, createdAt);
    }
}
