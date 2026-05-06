package com.lab.bank.domain.port;

import com.lab.bank.domain.model.Transfer;
import com.lab.bank.domain.model.TransferStatus;

import java.util.Optional;
import java.util.UUID;

public interface TransferRepository {
    void save(Transfer transfer);
    Optional<Transfer> findById(UUID id);
    Optional<Transfer> findByIdempotencyKey(String key);
    void updateStatus(UUID id, TransferStatus status);
}
