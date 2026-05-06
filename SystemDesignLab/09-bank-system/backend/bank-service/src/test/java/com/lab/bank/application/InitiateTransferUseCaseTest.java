package com.lab.bank.application;

import com.lab.bank.application.saga.TransferSagaOrchestrator;
import com.lab.bank.application.usecase.InitiateTransferUseCase;
import com.lab.bank.domain.model.Transfer;
import com.lab.bank.domain.model.TransferStatus;
import com.lab.bank.domain.port.TransferRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class InitiateTransferUseCaseTest {

    private final TransferRepository transferRepository = mock(TransferRepository.class);
    private final TransferSagaOrchestrator orchestrator = mock(TransferSagaOrchestrator.class);
    private final InitiateTransferUseCase useCase = new InitiateTransferUseCase(transferRepository, orchestrator);

    private final UUID from = UUID.randomUUID();
    private final UUID to = UUID.randomUUID();
    private final BigDecimal amount = BigDecimal.valueOf(100);
    private final String key = UUID.randomUUID().toString();

    @Test
    void saves_transfer_and_executes_saga() {
        when(transferRepository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
        Transfer completed = Transfer.pending(from, to, amount, key).withStatus(TransferStatus.COMPLETED);
        when(orchestrator.execute(any())).thenReturn(completed);

        Transfer result = useCase.initiate(from, to, amount, key);

        verify(transferRepository).save(any(Transfer.class));
        verify(orchestrator).execute(any(Transfer.class));
        assertThat(result.status()).isEqualTo(TransferStatus.COMPLETED);
    }

    @Test
    void returns_existing_transfer_for_duplicate_idempotency_key() {
        Transfer existing = new Transfer(UUID.randomUUID(), from, to, amount,
                TransferStatus.COMPLETED, key, Instant.now());
        when(transferRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(existing));

        Transfer result = useCase.initiate(from, to, amount, key);

        assertThat(result).isEqualTo(existing);
        verify(transferRepository, never()).save(any());
        verify(orchestrator, never()).execute(any());
    }
}
