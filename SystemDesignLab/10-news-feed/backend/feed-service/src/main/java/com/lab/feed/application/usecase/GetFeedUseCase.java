package com.lab.feed.application.usecase;

import com.lab.feed.application.service.RankingService;
import com.lab.feed.domain.model.FeedItem;
import com.lab.feed.domain.model.Post;
import com.lab.feed.domain.model.User;
import com.lab.feed.domain.port.FeedPort;
import com.lab.feed.domain.port.FollowRepository;
import com.lab.feed.domain.port.PostRepository;
import com.lab.feed.domain.port.UserRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

// Pattern: Hybrid Fan-out read — merges Redis pre-built feed with celebrity posts pulled from DB
public class GetFeedUseCase {

    private static final int REDIS_FETCH_LIMIT = 60;
    private static final int CELEBRITY_FETCH_LIMIT = 20;

    private final FeedPort feedPort;
    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final RankingService rankingService;

    public GetFeedUseCase(FeedPort feedPort, PostRepository postRepository,
                          FollowRepository followRepository, UserRepository userRepository,
                          RankingService rankingService) {
        this.feedPort = feedPort;
        this.postRepository = postRepository;
        this.followRepository = followRepository;
        this.userRepository = userRepository;
        this.rankingService = rankingService;
    }

    public List<FeedItem> getFeed(String userEmail, int page, int size) {
        // Step 1: pre-built fan-out posts from Redis
        List<UUID> postIds = feedPort.getTopPostIds(userEmail, REDIS_FETCH_LIMIT);
        List<Post> redisPosts = postRepository.findAllByIds(postIds);

        // Step 2: celebrity posts merged at read time
        List<String> followeeEmails = followRepository.findFolloweeIds(userEmail);
        List<User> celebrities = userRepository.findAllByEmails(followeeEmails).stream()
                .filter(User::isCelebrity)
                .toList();

        List<Post> allPosts = new ArrayList<>(redisPosts);
        for (User celebrity : celebrities) {
            allPosts.addAll(postRepository.findRecentByAuthorId(celebrity.email(), CELEBRITY_FETCH_LIMIT));
        }

        // Step 3: rank, deduplicate, paginate
        return allPosts.stream()
                .distinct()
                .map(p -> FeedItem.from(p, rankingService.score(p)))
                .sorted(Comparator.comparingDouble(FeedItem::score).reversed())
                .skip((long) page * size)
                .limit(size)
                .toList();
    }
}
