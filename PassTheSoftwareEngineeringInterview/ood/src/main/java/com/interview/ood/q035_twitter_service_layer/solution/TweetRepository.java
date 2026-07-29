package com.interview.ood.q035_twitter_service_layer.solution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Design decision: two maps — byId for direct lookup (NotificationService needs this to
// resolve a Like's tweet author) and byAuthor as a secondary index so FeedService doesn't
// scan every tweet ever posted to build one user's timeline.
// Rejected alternative: a single Map<String, List<Tweet>> keyed by authorId only, dropping
// byId — rejected because NotificationService.onLiked() (a later task) needs O(1) lookup by
// tweetId to resolve a like's author, which an author-keyed-only map can't provide without a
// full scan.
// What NOT to do: don't rebuild byAuthor by filtering byId.values() on every read — that's
// the O(n) scan this secondary index exists specifically to avoid.
public class TweetRepository {

    private final Map<String, Tweet> byId = new HashMap<>();
    private final Map<String, List<Tweet>> byAuthor = new HashMap<>();

    public Tweet save(Tweet tweet) {
        byId.put(tweet.id(), tweet);
        byAuthor.computeIfAbsent(tweet.authorId(), k -> new ArrayList<>()).add(tweet);
        return tweet;
    }

    public Optional<Tweet> findById(String tweetId) {
        return Optional.ofNullable(byId.get(tweetId));
    }

    public List<Tweet> findByAuthorId(String authorId) {
        return List.copyOf(byAuthor.getOrDefault(authorId, List.of()));
    }
}
