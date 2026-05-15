package com.lab.feed.application.usecase;

import com.lab.feed.application.service.FanOutService;
import com.lab.feed.application.service.RankingService;
import com.lab.feed.domain.model.Post;
import com.lab.feed.domain.model.PostNotFoundException;
import com.lab.feed.domain.port.PostRepository;

import java.util.UUID;

public class LikePostUseCase {

    private final PostRepository postRepository;
    private final FanOutService fanOutService;
    private final RankingService rankingService;

    public LikePostUseCase(PostRepository postRepository, FanOutService fanOutService,
                            RankingService rankingService) {
        this.postRepository = postRepository;
        this.fanOutService = fanOutService;
        this.rankingService = rankingService;
    }

    public Post like(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        Post liked = postRepository.save(post.withLikeCount(post.likeCount() + 1));
        double newScore = rankingService.score(liked);
        // Only re-scores regular-user feeds; celebrity feeds rebuilt on read anyway
        fanOutService.rescorePost(liked, newScore);
        return liked;
    }
}
