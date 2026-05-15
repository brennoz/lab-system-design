package com.lab.feed.domain.port;

import com.lab.feed.domain.model.User;

import java.util.List;
import java.util.Optional;

// Port: secondary (driven) — persistence boundary for users
public interface UserRepository {
    User save(User user);
    Optional<User> findByEmail(String email);
    List<User> findAllByEmails(List<String> emails);
    void incrementFollowerCount(String email);
    void decrementFollowerCount(String email);
}
