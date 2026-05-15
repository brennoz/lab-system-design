package com.lab.feed.application.service;

import com.lab.feed.domain.model.Post;
import com.lab.feed.domain.model.User;
import com.lab.feed.domain.port.FeedPort;
import com.lab.feed.domain.port.FollowRepository;

import java.util.List;

// Pattern: Hybrid Fan-out — write for regular users (<10k), skip for celebrities (≥10k)
public class FanOutService {

    private static final int FEED_CAP = 500;

    private final FollowRepository followRepository;
    private final FeedPort feedPort;
    private final RankingService rankingService;

    public FanOutService(FollowRepository followRepository, FeedPort feedPort,
                         RankingService rankingService) {
        this.followRepository = followRepository;
        this.feedPort = feedPort;
        this.rankingService = rankingService;
    }

    public void fanOut(Post post, User author) {
        if (author.isCelebrity()) {
            // Fan-out on Read: celebrity posts merged at read time — no Redis writes
            return;
        }
        double score = rankingService.score(post);
        List<String> followerIds = followRepository.findFollowerIds(author.email());
        for (String followerId : followerIds) {
            feedPort.addToFeed(followerId, post.id(), score);
            feedPort.trimFeed(followerId, FEED_CAP);
        }
    }

    // Called on like: re-scores existing feed entries for regular-author posts
    public void rescorePost(Post post, double newScore) {
        List<String> followerIds = followRepository.findFollowerIds(post.authorId());
        for (String followerId : followerIds) {
            feedPort.addToFeed(followerId, post.id(), newScore);
        }
    }
}
