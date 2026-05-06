package com.lab.bank.domain.model;

public enum TransferStatus {
    PENDING,
    DEBITING,
    CREDITING,
    COMPLETED,
    COMPENSATING,
    COMPENSATED,
    FAILED
}
