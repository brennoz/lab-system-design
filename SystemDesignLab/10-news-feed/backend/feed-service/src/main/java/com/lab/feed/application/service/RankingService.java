package com.lab.feed.application.service;

import com.lab.feed.domain.model.Post;

// Algorithm: additive hybrid score — epoch + like boost fits Redis sorted set double with no precision loss
public class RankingService {

    // Why 1000: ~16 min per like; enough to surface popular posts without burying recency
    static final long LIKE_BOOST_SECONDS = 1_000L;

    public double score(Post post) {
        return post.createdAt().getEpochSecond() + (long) post.likeCount() * LIKE_BOOST_SECONDS;
    }
}
