package com.lab.bank.api.dto;

import com.lab.bank.domain.model.Transfer;
import com.lab.bank.domain.model.TransferStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferResponse(UUID transferId, UUID fromAccountId, UUID toAccountId,
                               BigDecimal amount, TransferStatus status) {
    public static TransferResponse from(Transfer t) {
        return new TransferResponse(t.id(), t.fromAccountId(), t.toAccountId(), t.amount(), t.status());
    }
}
