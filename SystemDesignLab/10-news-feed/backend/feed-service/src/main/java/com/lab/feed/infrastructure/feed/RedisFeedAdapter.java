package com.lab.feed.infrastructure.feed;

import com.lab.feed.domain.port.FeedPort;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Set;
import java.util.UUID;

// Adapter: Redis sorted set — key=feed:{userId}, score=hybrid_rank, member=postId
public class RedisFeedAdapter implements FeedPort {

    private static final String KEY_PREFIX = "feed:";

    private final StringRedisTemplate redis;

    public RedisFeedAdapter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void addToFeed(String userId, UUID postId, double score) {
        // Algorithm: ZADD O(log N) — upserts member with new score
        redis.opsForZSet().add(key(userId), postId.toString(), score);
    }

    @Override
    public void removeFromFeed(String userId, UUID postId) {
        redis.opsForZSet().remove(key(userId), postId.toString());
    }

    @Override
    public List<UUID> getTopPostIds(String userId, int limit) {
        // Algorithm: ZREVRANGE O(log N + M) — highest score first
        Set<String> members = redis.opsForZSet().reverseRange(key(userId), 0, limit - 1);
        if (members == null) return List.of();
        return members.stream().map(UUID::fromString).toList();
    }

    @Override
    public void trimFeed(String userId, int maxSize) {
        // Algorithm: ZREMRANGEBYRANK — removes lowest-scored entries beyond cap
        redis.opsForZSet().removeRange(key(userId), 0, -(maxSize + 1));
    }

    private String key(String userId) {
        return KEY_PREFIX + userId;
    }
}
