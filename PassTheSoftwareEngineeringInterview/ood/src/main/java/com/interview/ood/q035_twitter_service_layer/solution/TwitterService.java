package com.interview.ood.q035_twitter_service_layer.solution;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// Pattern: Facade — the single entry point coordinating five independently-testable services.
// Its only Observer-related job is wiring the subscription ONCE at construction
// (socialGraph.addObserver / likeService.addObserver); it never calls
// notificationService.onFollowed()/onLiked() itself. That distinction matters: if the facade
// published events, adding a second Subject later would mean editing TwitterService every
// time, instead of the new Subject just registering the same observer.
// What NOT to do: don't add new business logic methods directly on TwitterService that
// duplicate what SocialGraph/LikeService/FeedService already do — the facade should stay a
// thin coordinator, not accumulate its own domain logic.
public class TwitterService {

    private static final int DEFAULT_TIMELINE_LIMIT = 50;
    private static final int MAX_CONTENT_LENGTH = 280;

    private final UserRepository userRepository;
    private final TweetRepository tweetRepository;
    private final SocialGraph socialGraph;
    private final FeedService feedService;
    private final LikeService likeService;
    private final NotificationService notificationService;

    public TwitterService() {
        this.userRepository = new UserRepository();
        this.tweetRepository = new TweetRepository();
        this.socialGraph = new SocialGraph();
        this.likeService = new LikeService();
        this.feedService = new FeedService(socialGraph, tweetRepository);
        this.notificationService = new NotificationService(userRepository, tweetRepository);

        // Subscribe once, here — this is the only place SocialGraph/LikeService and
        // NotificationService know about each other.
        socialGraph.addObserver(notificationService);
        likeService.addObserver(notificationService);
    }

    public User register(String username) {
        User user = new User(UUID.randomUUID().toString(), username, "");
        return userRepository.save(user);
    }

    public Tweet post(String userId, String content) {
        if (content == null || content.isBlank() || content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException(
                    "Tweet content must be 1-" + MAX_CONTENT_LENGTH + " characters");
        }
        Tweet tweet = new Tweet(UUID.randomUUID().toString(), userId, content, null, Instant.now());
        return tweetRepository.save(tweet);
    }

    public Follow follow(String followerId, String followeeId) {
        return socialGraph.follow(followerId, followeeId);
    }

    public Like like(String userId, String tweetId) {
        return likeService.like(userId, tweetId);
    }

    public List<Tweet> getTimeline(String userId) {
        return feedService.getTimeline(userId, DEFAULT_TIMELINE_LIMIT);
    }

    // Accessors for tests — expose internal services directly rather than duplicating every
    // query method on the facade (isFollowing, getLikeCount, getNotifications, ...). Acceptable
    // in an interview-scale system; a production facade might narrow this surface.
    public SocialGraph getSocialGraph() {
        return socialGraph;
    }

    public LikeService getLikeService() {
        return likeService;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }
}
