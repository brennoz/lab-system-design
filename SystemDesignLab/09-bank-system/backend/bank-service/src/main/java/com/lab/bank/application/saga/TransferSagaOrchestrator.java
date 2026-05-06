package com.lab.bank.application.saga;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lab.bank.domain.model.Account;
import com.lab.bank.domain.model.AccountEvent;
import com.lab.bank.domain.model.AccountEventType;
import com.lab.bank.domain.model.AccountNotFoundException;
import com.lab.bank.domain.model.OptimisticLockException;
import com.lab.bank.domain.model.OutboxEvent;
import com.lab.bank.domain.model.Transfer;
import com.lab.bank.domain.model.TransferStatus;
import com.lab.bank.domain.port.AccountEventRepository;
import com.lab.bank.domain.port.AccountRepository;
import com.lab.bank.domain.port.OutboxPort;
import com.lab.bank.domain.port.TransferRepository;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.UUID;

// Pattern: Orchestration Saga — orchestrator drives TX1(debit) → TX2(credit) → TX3(complete) imperatively.
// Each step runs in its own TransactionTemplate so earlier committed steps survive later failures.
// In a multi-service saga each step would be a separate service; here they share one DB.
public class TransferSagaOrchestrator {

    private final AccountRepository accountRepository;
    private final AccountEventRepository accountEventRepository;
    private final TransferRepository transferRepository;
    private final OutboxPort outboxPort;
    private final ObjectMapper mapper;
    private final TransactionTemplate txTemplate;

    public TransferSagaOrchestrator(AccountRepository accountRepository,
                                    AccountEventRepository accountEventRepository,
                                    TransferRepository transferRepository,
                                    OutboxPort outboxPort,
                                    ObjectMapper mapper,
                                    TransactionTemplate txTemplate) {
        this.accountRepository = accountRepository;
        this.accountEventRepository = accountEventRepository;
        this.transferRepository = transferRepository;
        this.outboxPort = outboxPort;
        this.mapper = mapper;
        this.txTemplate = txTemplate;
    }

    public Transfer execute(Transfer transfer) {
        UUID id = transfer.id();

        // ── TX1: debit source account ─────────────────────────────────────────
        boolean debitOk = Boolean.TRUE.equals(txTemplate.execute(status -> {
            try {
                Account from = accountRepository.findById(transfer.fromAccountId())
                        .orElseThrow(() -> new AccountNotFoundException(transfer.fromAccountId()));
                Account debited = from.debit(transfer.amount());
                if (!accountRepository.updateBalanceWithVersion(from.id(), debited.balance(), from.version()))
                    throw new OptimisticLockException(from.id());
                accountEventRepository.save(AccountEvent.of(from.id(), AccountEventType.TRANSFER_DEBITED,
                        transfer.amount(), from.balance(), debited.balance(), id));
                transferRepository.updateStatus(id, TransferStatus.DEBITING);
                return true;
            } catch (Exception e) {
                status.setRollbackOnly();
                return false;
            }
        }));

        if (!debitOk) {
            txTemplate.executeWithoutResult(s -> {
                transferRepository.updateStatus(id, TransferStatus.FAILED);
                outboxPort.save(OutboxEvent.unpublished(id, "TRANSFER_FAILED", toJson(id, "TRANSFER_FAILED")));
            });
            return transfer.withStatus(TransferStatus.FAILED);
        }

        // ── TX2: credit destination account ──────────────────────────────────
        boolean creditOk = Boolean.TRUE.equals(txTemplate.execute(status -> {
            try {
                Account to = accountRepository.findById(transfer.toAccountId())
                        .orElseThrow(() -> new AccountNotFoundException(transfer.toAccountId()));
                Account credited = to.credit(transfer.amount());
                if (!accountRepository.updateBalanceWithVersion(to.id(), credited.balance(), to.version()))
                    throw new OptimisticLockException(to.id());
                accountEventRepository.save(AccountEvent.of(to.id(), AccountEventType.TRANSFER_CREDITED,
                        transfer.amount(), to.balance(), credited.balance(), id));
                transferRepository.updateStatus(id, TransferStatus.CREDITING);
                return true;
            } catch (Exception e) {
                status.setRollbackOnly();
                return false;
            }
        }));

        if (!creditOk) {
            // Compensation: re-credit source account
            txTemplate.executeWithoutResult(s -> {
                transferRepository.updateStatus(id, TransferStatus.COMPENSATING);
                Account from = accountRepository.findById(transfer.fromAccountId())
                        .orElseThrow(() -> new AccountNotFoundException(transfer.fromAccountId()));
                Account compensated = from.credit(transfer.amount());
                if (!accountRepository.updateBalanceWithVersion(from.id(), compensated.balance(), from.version()))
                    throw new OptimisticLockException(from.id());
                accountEventRepository.save(AccountEvent.of(from.id(), AccountEventType.TRANSFER_DEBIT_REVERSED,
                        transfer.amount(), from.balance(), compensated.balance(), id));
                transferRepository.updateStatus(id, TransferStatus.COMPENSATED);
                outboxPort.save(OutboxEvent.unpublished(id, "TRANSFER_FAILED", toJson(id, "TRANSFER_FAILED")));
            });
            return transfer.withStatus(TransferStatus.COMPENSATED);
        }

        // ── TX3: complete ─────────────────────────────────────────────────────
        txTemplate.executeWithoutResult(s -> {
            transferRepository.updateStatus(id, TransferStatus.COMPLETED);
            outboxPort.save(OutboxEvent.unpublished(id, "TRANSFER_COMPLETED", toJson(id, "TRANSFER_COMPLETED")));
        });
        return transfer.withStatus(TransferStatus.COMPLETED);
    }

    private String toJson(UUID transferId, String eventType) {
        try {
            return mapper.writeValueAsString(Map.of(
                    "eventType", eventType,
                    "transferId", transferId.toString()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize outbox payload", e);
        }
    }
}
