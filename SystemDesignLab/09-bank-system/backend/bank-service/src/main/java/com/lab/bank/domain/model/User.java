package com.lab.bank.domain.model;

import java.time.Instant;
import java.util.UUID;

public record User(UUID id, String email, String passwordHash, Instant createdAt) {

    public static User of(String email, String passwordHash) {
        return new User(UUID.randomUUID(), email, passwordHash, Instant.now());
    }
}
