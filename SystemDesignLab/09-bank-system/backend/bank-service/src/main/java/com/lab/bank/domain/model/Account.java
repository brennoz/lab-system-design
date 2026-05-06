package com.lab.bank.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// Aggregate: balance snapshot + version for optimistic locking; event log is separate
public record Account(UUID id, String ownerId, BigDecimal balance, long version, Instant createdAt) {

    public static Account open(String ownerId) {
        return new Account(UUID.randomUUID(), ownerId, BigDecimal.ZERO, 0L, Instant.now());
    }

    public Account debit(BigDecimal amount) {
        if (balance.compareTo(amount) < 0) throw new InsufficientFundsException(id, balance, amount);
        return new Account(id, ownerId, balance.subtract(amount), version, createdAt);
    }

    public Account credit(BigDecimal amount) {
        return new Account(id, ownerId, balance.add(amount), version, createdAt);
    }
}
