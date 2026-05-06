package com.lab.bank.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

// Aggregate: saga state machine; idempotencyKey stored with UNIQUE constraint
public record Transfer(UUID id, UUID fromAccountId, UUID toAccountId, BigDecimal amount,
                       TransferStatus status, String idempotencyKey, Instant createdAt) {

    public static Transfer pending(UUID fromAccountId, UUID toAccountId,
                                   BigDecimal amount, String idempotencyKey) {
        return new Transfer(UUID.randomUUID(), fromAccountId, toAccountId, amount,
                TransferStatus.PENDING, idempotencyKey, Instant.now());
    }

    public Transfer withStatus(TransferStatus status) {
        return new Transfer(id, fromAccountId, toAccountId, amount, status, idempotencyKey, createdAt);
    }
}
