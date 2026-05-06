package com.lab.bank.application.usecase;

import com.lab.bank.application.saga.TransferSagaOrchestrator;
import com.lab.bank.domain.model.Transfer;
import com.lab.bank.domain.port.TransferRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

// Pattern: Idempotency — duplicate key returns existing transfer without re-executing saga
public class InitiateTransferUseCase {

    private final TransferRepository transferRepository;
    private final TransferSagaOrchestrator orchestrator;

    public InitiateTransferUseCase(TransferRepository transferRepository,
                                   TransferSagaOrchestrator orchestrator) {
        this.transferRepository = transferRepository;
        this.orchestrator = orchestrator;
    }

    public Transfer initiate(UUID fromAccountId, UUID toAccountId,
                             BigDecimal amount, String idempotencyKey) {
        Optional<Transfer> existing = transferRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) return existing.get();

        Transfer transfer = Transfer.pending(fromAccountId, toAccountId, amount, idempotencyKey);
        transferRepository.save(transfer);
        return orchestrator.execute(transfer);
    }
}
