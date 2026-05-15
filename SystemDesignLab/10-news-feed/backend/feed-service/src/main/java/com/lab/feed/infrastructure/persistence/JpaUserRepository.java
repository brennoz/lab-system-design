package com.lab.feed.infrastructure.persistence;

import com.lab.feed.domain.model.User;
import com.lab.feed.domain.port.UserRepository;

import java.util.List;
import java.util.Optional;

// Adapter: maps between User domain record and UserJpaEntity
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository delegate;

    public JpaUserRepository(SpringDataUserRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = new UserJpaEntity(user.id(), user.email(), user.passwordHash(),
                user.followerCount(), user.createdAt());
        UserJpaEntity saved = delegate.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return delegate.findByEmail(email).map(this::toDomain);
    }

    @Override
    public List<User> findAllByEmails(List<String> emails) {
        return delegate.findAllByEmailIn(emails).stream().map(this::toDomain).toList();
    }

    @Override
    public void incrementFollowerCount(String email) {
        delegate.incrementFollowerCount(email);
    }

    @Override
    public void decrementFollowerCount(String email) {
        delegate.decrementFollowerCount(email);
    }

    private User toDomain(UserJpaEntity e) {
        return new User(e.getId(), e.getEmail(), e.getPasswordHash(), e.getFollowerCount(), e.getCreatedAt());
    }
}
