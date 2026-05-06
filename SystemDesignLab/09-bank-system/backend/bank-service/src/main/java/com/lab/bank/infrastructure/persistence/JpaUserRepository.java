package com.lab.bank.infrastructure.persistence;

import com.lab.bank.domain.model.User;
import com.lab.bank.domain.port.UserRepository;

import java.util.Optional;

public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository delegate;

    public JpaUserRepository(SpringDataUserRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public void save(User user) {
        delegate.save(new UserJpaEntity(user.id(), user.email(), user.passwordHash(), user.createdAt()));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return delegate.findByEmail(email)
                .map(e -> new User(e.getId(), e.getEmail(), e.getPasswordHash(), e.getCreatedAt()));
    }
}
