package com.lab.feed.domain.model;

import java.time.Instant;

// Value object: assembled at read time from Post + ranking score; not persisted
public record FeedItem(String postId, String authorId, String content, int likeCount,
                       double score, Instant createdAt) {

    public static FeedItem from(Post post, double score) {
        return new FeedItem(post.id().toString(), post.authorId(), post.content(),
                post.likeCount(), score, post.createdAt());
    }
}
