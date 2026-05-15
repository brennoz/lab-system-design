package com.lab.feed.domain.port;

import java.util.List;
import java.util.UUID;

// Port: secondary (driven) — Redis sorted set operations for timeline storage
public interface FeedPort {
    // Algorithm: ZADD O(log N) — score encodes hybrid rank (epoch + like boost)
    void addToFeed(String userId, UUID postId, double score);
    void removeFromFeed(String userId, UUID postId);
    // Algorithm: ZREVRANGE O(log N + M) — returns top-scored post IDs
    List<UUID> getTopPostIds(String userId, int limit);
    // Algorithm: ZREMRANGEBYRANK — trims inbox to cap, prevents unbounded growth
    void trimFeed(String userId, int maxSize);
}
