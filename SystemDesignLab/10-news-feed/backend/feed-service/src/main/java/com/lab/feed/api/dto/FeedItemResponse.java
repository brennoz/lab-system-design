package com.lab.feed.api.dto;

import com.lab.feed.domain.model.FeedItem;

import java.time.Instant;

public record FeedItemResponse(String postId, String authorId, String content,
                                int likeCount, double score, Instant createdAt) {

    public static FeedItemResponse from(FeedItem item) {
        return new FeedItemResponse(item.postId(), item.authorId(), item.content(),
                item.likeCount(), item.score(), item.createdAt());
    }
}
