package com.lab.bank.domain.model;

import java.util.UUID;

// Why: manual version check returns 0 rows updated on concurrent modification — must throw to trigger saga compensation
public class OptimisticLockException extends RuntimeException {
    public OptimisticLockException(UUID accountId) {
        super("Concurrent modification detected on account: " + accountId);
    }
}
