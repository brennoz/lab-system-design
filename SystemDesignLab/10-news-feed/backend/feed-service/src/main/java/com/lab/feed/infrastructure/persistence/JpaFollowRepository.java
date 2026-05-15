package com.lab.feed.infrastructure.persistence;

import com.lab.feed.domain.model.Follow;
import com.lab.feed.domain.port.FollowRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Adapter: maps between Follow domain record and FollowJpaEntity
public class JpaFollowRepository implements FollowRepository {

    private final SpringDataFollowRepository delegate;

    public JpaFollowRepository(SpringDataFollowRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Follow save(Follow follow) {
        FollowJpaEntity entity = new FollowJpaEntity(follow.id(), follow.followerId(),
                follow.followeeId(), follow.createdAt());
        FollowJpaEntity saved = delegate.save(entity);
        return toDomain(saved);
    }

    @Override
    public void delete(UUID id) {
        delegate.deleteById(id);
    }

    @Override
    public List<String> findFollowerIds(String followeeId) {
        return delegate.findFollowerIds(followeeId);
    }

    @Override
    public List<String> findFolloweeIds(String followerId) {
        return delegate.findFolloweeIds(followerId);
    }

    @Override
    public Optional<Follow> findByFollowerAndFollowee(String followerId, String followeeId) {
        return delegate.findByFollowerIdAndFolloweeId(followerId, followeeId).map(this::toDomain);
    }

    private Follow toDomain(FollowJpaEntity e) {
        return new Follow(e.getId(), e.getFollowerId(), e.getFolloweeId(), e.getCreatedAt());
    }
}
