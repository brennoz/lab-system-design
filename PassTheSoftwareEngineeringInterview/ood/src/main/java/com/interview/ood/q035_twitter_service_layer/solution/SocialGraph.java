package com.interview.ood.q035_twitter_service_layer.solution;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Pattern: Observer (Subject side) — SocialGraph is the Subject for follow events, holding
// its own observer list and calling notifyObservers() at the exact point state changes
// (inside follow(), after both adjacency maps update). This is the textbook GoF shape: the
// object whose state changes publishes, rather than a facade manually re-announcing state
// changes after the fact.
// Rejected alternative: let TwitterService (the facade) call notificationService.onFollowed()
// itself after delegating to SocialGraph.follow(). Simpler wiring, but then SocialGraph isn't
// really a Subject — it's just a class the facade happens to call before also calling
// something else. That doesn't demonstrate Observer; it's two sequential method calls.
//
// Why two adjacency maps (following + followers), not one: getFollowers() and getFollowing()
// are both O(1) set lookups this way. A single "following" map would make getFollowers() an
// O(n) scan over every user's following set — the exact query q032's push-based Observer
// model couldn't answer at all without walking every User's private observer list.
public class SocialGraph {

    private final Map<String, Set<String>> following = new HashMap<>();
    private final Map<String, Set<String>> followers = new HashMap<>();
    private final List<TwitterEventObserver> observers = new ArrayList<>();

    public void addObserver(TwitterEventObserver observer) {
        observers.add(observer);
    }

    public Follow follow(String followerId, String followeeId) {
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("A user cannot follow themselves: " + followerId);
        }
        following.computeIfAbsent(followerId, k -> new HashSet<>()).add(followeeId);
        followers.computeIfAbsent(followeeId, k -> new HashSet<>()).add(followerId);

        Follow event = new Follow(followerId, followeeId, Instant.now());
        // Notify AFTER both maps are consistent — an observer reacting to onFollowed() (e.g.
        // NotificationService resolving usernames) must see a fully-updated graph, not a
        // half-written one.
        for (TwitterEventObserver observer : observers) {
            observer.onFollowed(event);
        }
        return event;
    }

    public void unfollow(String followerId, String followeeId) {
        Set<String> followeeSet = following.get(followerId);
        if (followeeSet != null) {
            followeeSet.remove(followeeId);
        }
        Set<String> followerSet = followers.get(followeeId);
        if (followerSet != null) {
            followerSet.remove(followerId);
        }
        // What NOT to do: don't notify observers here or try to retract a past notification.
        // Notifications are an append-only event log ("alice followed you" stays true as
        // history even after alice unfollows) — same reasoning as Follow/Like being explicit,
        // immutable entities rather than mutable state.
    }

    public Set<String> getFollowing(String userId) {
        return Set.copyOf(following.getOrDefault(userId, Set.of()));
    }

    public Set<String> getFollowers(String userId) {
        return Set.copyOf(followers.getOrDefault(userId, Set.of()));
    }

    public boolean isFollowing(String followerId, String followeeId) {
        return following.getOrDefault(followerId, Set.of()).contains(followeeId);
    }

    public int getFollowerCount(String userId) {
        return followers.getOrDefault(userId, Set.of()).size();
    }
}
