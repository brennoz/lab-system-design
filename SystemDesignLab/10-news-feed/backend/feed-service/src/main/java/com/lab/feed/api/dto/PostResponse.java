package com.lab.feed.api.dto;

import com.lab.feed.domain.model.Post;

import java.time.Instant;
import java.util.UUID;

public record PostResponse(UUID postId, String authorId, String content, int likeCount, Instant createdAt) {

    public static PostResponse from(Post post) {
        return new PostResponse(post.id(), post.authorId(), post.content(),
                post.likeCount(), post.createdAt());
    }
}
