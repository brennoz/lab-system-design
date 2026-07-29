package com.interview.ood.q035_twitter_service_layer.solution;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Pattern: Observer (Subject side) — same shape as SocialGraph, but for like events.
// Two Subjects rather than one shared "TwitterSubject" superclass: SocialGraph and LikeService
// have nothing else in common (different state, different guard conditions), and a shared
// base class would only exist to host the observer list + notify loop — four lines of
// duplication that isn't worth the coupling of forcing both through one supertype.
// What NOT to do: don't call notificationService.onLiked() directly instead of iterating this
// class's own observer list — that bypasses the Observer contract and would silently stop
// working the moment a second observer needs to react to likes too.
public class LikeService {

    private final Map<String, Like> likes = new HashMap<>();
    private final Map<String, Set<String>> likesByTweet = new HashMap<>();
    private final List<TwitterEventObserver> observers = new ArrayList<>();

    public void addObserver(TwitterEventObserver observer) {
        observers.add(observer);
    }

    public Like like(String userId, String tweetId) {
        String key = userId + ":" + tweetId;
        if (likes.containsKey(key)) {
            throw new IllegalStateException("User " + userId + " already liked tweet " + tweetId);
        }
        Like event = new Like(userId, tweetId, Instant.now());
        likes.put(key, event);
        likesByTweet.computeIfAbsent(tweetId, k -> new HashSet<>()).add(userId);

        for (TwitterEventObserver observer : observers) {
            observer.onLiked(event);
        }
        return event;
    }

    public void unlike(String userId, String tweetId) {
        String key = userId + ":" + tweetId;
        likes.remove(key);
        Set<String> likers = likesByTweet.get(tweetId);
        if (likers != null) {
            likers.remove(userId);
        }
        // Same event-log reasoning as SocialGraph.unfollow(): no notification retraction here.
    }

    public int getLikeCount(String tweetId) {
        return likesByTweet.getOrDefault(tweetId, Set.of()).size();
    }
}
