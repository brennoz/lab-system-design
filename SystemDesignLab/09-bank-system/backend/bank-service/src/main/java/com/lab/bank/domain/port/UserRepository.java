package com.lab.bank.domain.port;

import com.lab.bank.domain.model.User;

import java.util.Optional;

public interface UserRepository {
    void save(User user);
    Optional<User> findByEmail(String email);
}
