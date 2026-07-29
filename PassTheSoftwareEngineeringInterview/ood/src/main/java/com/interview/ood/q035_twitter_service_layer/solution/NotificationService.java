package com.interview.ood.q035_twitter_service_layer.solution;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// Pattern: Observer (concrete Observer side) — subscribes to both SocialGraph and LikeService
// as a TwitterEventObserver. This is the whole reason the interface exists: one class reacting
// to two different Subjects' state changes without either Subject knowing what happens next.
//
// Why NotificationService needs UserRepository + TweetRepository: the events it receives
// (Follow, Like) only carry IDs. Turning "u-123 followed u-456" into "alice followed you"
// requires a username lookup; turning a Like into "who gets notified" requires resolving the
// tweet's author. Keeping that resolution here (not in LikeService/SocialGraph) means the
// Subjects stay ignorant of notifications entirely — they just publish raw domain events.
//
// Rejected alternative: have SocialGraph/LikeService construct fully-formed Notification
// objects themselves and hand them to a generic Consumer<Notification>. Rejected because that
// couples both Subjects to Notification's shape (recipient resolution, message formatting) —
// passing the raw Follow/Like event instead keeps Subjects agnostic of what any observer does
// with it, which is the entire point of using Observer here.
//
// What NOT to do: don't have NotificationService push into FeedService's timeline directly
// (e.g. prepend the new tweet to a cached feed on every onFollowed/onLiked). That resurrects
// exactly the push-based Observer feed q032 already demonstrates doesn't scale for arbitrary
// historical queries (can't ask "show me tweets since 3 days ago" from a live-mutated cache).
// Notifications and the timeline are separate concerns solved separately: timeline stays pull
// (FeedService queries on demand), notifications stay push (this class reacts to events).
public class NotificationService implements TwitterEventObserver {

    private final UserRepository userRepository;
    private final TweetRepository tweetRepository;
    // Deque + addFirst, same shape as q032's Feed — O(1) prepend for "newest first" ordering,
    // instead of ArrayList.add(0, ...) which is O(n) per insert.
    private final Map<String, Deque<Notification>> byRecipient = new HashMap<>();

    public NotificationService(UserRepository userRepository, TweetRepository tweetRepository) {
        this.userRepository = userRepository;
        this.tweetRepository = tweetRepository;
    }

    @Override
    public void onFollowed(Follow follow) {
        String actorName = userRepository.findById(follow.followerId())
                .map(User::username)
                .orElse(follow.followerId());
        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                follow.followeeId(),
                follow.followerId(),
                NotificationType.FOLLOW,
                actorName + " followed you",
                Instant.now());
        store(notification);
    }

    @Override
    public void onLiked(Like like) {
        Optional<Tweet> tweet = tweetRepository.findById(like.tweetId());
        if (tweet.isEmpty()) {
            return;
        }
        String recipientId = tweet.get().authorId();
        // What NOT to do: notify yourself when you like your own tweet. Real product
        // behavior, and a genuine edge case this test suite covers — silently skipping it
        // here is the fix, not a defensive null-check elsewhere.
        if (recipientId.equals(like.userId())) {
            return;
        }
        String actorName = userRepository.findById(like.userId())
                .map(User::username)
                .orElse(like.userId());
        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                recipientId,
                like.userId(),
                NotificationType.LIKE,
                actorName + " liked your tweet",
                Instant.now());
        store(notification);
    }

    private void store(Notification notification) {
        byRecipient.computeIfAbsent(notification.recipientId(), k -> new ArrayDeque<>())
                .addFirst(notification);
    }

    public List<Notification> getNotifications(String userId) {
        return List.copyOf(byRecipient.getOrDefault(userId, new ArrayDeque<>()));
    }
}
