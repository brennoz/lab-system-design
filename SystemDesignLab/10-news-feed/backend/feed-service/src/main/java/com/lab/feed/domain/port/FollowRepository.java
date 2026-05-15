package com.lab.feed.domain.port;

import com.lab.feed.domain.model.Follow;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Port: secondary (driven) — persistence boundary for follow relationships
public interface FollowRepository {
    Follow save(Follow follow);
    void delete(UUID id);
    List<String> findFollowerIds(String followeeId);
    List<String> findFolloweeIds(String followerId);
    Optional<Follow> findByFollowerAndFollowee(String followerId, String followeeId);
}
