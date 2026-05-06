package com.lab.bank.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// Value object: immutable audit record; never updated after insert
public record AccountEvent(UUID id, UUID accountId, AccountEventType eventType,
                           BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter,
                           UUID correlationId, Instant createdAt) {

    public static AccountEvent of(UUID accountId, AccountEventType type, BigDecimal amount,
                                   BigDecimal before, BigDecimal after, UUID correlationId) {
        return new AccountEvent(UUID.randomUUID(), accountId, type, amount, before, after,
                correlationId, Instant.now());
    }
}
