package com.lab.bank.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lab.bank.application.saga.TransferSagaOrchestrator;
import com.lab.bank.domain.model.Account;
import com.lab.bank.domain.model.AccountEventType;
import com.lab.bank.domain.model.Transfer;
import com.lab.bank.domain.model.TransferStatus;
import com.lab.bank.domain.port.AccountEventRepository;
import com.lab.bank.domain.port.AccountRepository;
import com.lab.bank.domain.port.OutboxPort;
import com.lab.bank.domain.port.TransferRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TransferSagaOrchestratorTest {

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final AccountEventRepository accountEventRepository = mock(AccountEventRepository.class);
    private final TransferRepository transferRepository = mock(TransferRepository.class);
    private final OutboxPort outboxPort = mock(OutboxPort.class);
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    // No-op TransactionManager: runs callbacks without real DB transaction management
    private final TransactionTemplate txTemplate = new TransactionTemplate(
            new AbstractPlatformTransactionManager() {
                @Override protected Object doGetTransaction() { return new Object(); }
                @Override protected void doBegin(Object tx, TransactionDefinition def) {}
                @Override protected void doCommit(DefaultTransactionStatus s) {}
                @Override protected void doRollback(DefaultTransactionStatus s) {}
            });

    private final TransferSagaOrchestrator orchestrator = new TransferSagaOrchestrator(
            accountRepository, accountEventRepository, transferRepository, outboxPort, mapper, txTemplate);

    private Account account(UUID id, BigDecimal balance) {
        return new Account(id, "user-1", balance, 0L, Instant.now());
    }

    @Test
    void completes_transfer_debit_then_credit() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();
        Transfer transfer = Transfer.pending(fromId, toId, BigDecimal.valueOf(100), "key-1");

        when(accountRepository.findById(fromId)).thenReturn(Optional.of(account(fromId, BigDecimal.valueOf(500))));
        when(accountRepository.findById(toId)).thenReturn(Optional.of(account(toId, BigDecimal.valueOf(200))));
        when(accountRepository.updateBalanceWithVersion(any(), any(), anyLong())).thenReturn(true);

        Transfer result = orchestrator.execute(transfer);

        assertThat(result.status()).isEqualTo(TransferStatus.COMPLETED);

        ArgumentCaptor<AccountEventType> eventTypes = ArgumentCaptor.forClass(AccountEventType.class);
        // capture via save calls
        verify(accountRepository, times(2)).updateBalanceWithVersion(any(), any(), anyLong());
        verify(outboxPort).save(argThat(e -> e.eventType().equals("TRANSFER_COMPLETED")));
    }

    @Test
    void compensates_debit_when_credit_fails() {
        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();
        Transfer transfer = Transfer.pending(fromId, toId, BigDecimal.valueOf(100), "key-2");

        // debit succeeds
        when(accountRepository.findById(fromId)).thenReturn(Optional.of(account(fromId, BigDecimal.valueOf(500))));
        when(accountRepository.updateBalanceWithVersion(eq(fromId), any(), anyLong())).thenReturn(true);
        // credit fails — toAccount not found triggers compensation
        when(accountRepository.findById(toId)).thenThrow(new RuntimeException("to-account unavailable"));

        Transfer result = orchestrator.execute(transfer);

        assertThat(result.status()).isEqualTo(TransferStatus.COMPENSATED);
        // compensation re-credits from-account: updateBalanceWithVersion called twice (debit + compensate)
        verify(accountRepository, times(2)).updateBalanceWithVersion(eq(fromId), any(), anyLong());
        verify(outboxPort).save(argThat(e -> e.eventType().equals("TRANSFER_FAILED")));
    }
}
