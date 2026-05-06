package com.lab.bank.application;

import com.lab.bank.application.usecase.DepositUseCase;
import com.lab.bank.domain.model.Account;
import com.lab.bank.domain.model.AccountEvent;
import com.lab.bank.domain.model.AccountEventType;
import com.lab.bank.domain.port.AccountEventRepository;
import com.lab.bank.domain.port.AccountRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class DepositUseCaseTest {

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final AccountEventRepository accountEventRepository = mock(AccountEventRepository.class);
    private final DepositUseCase useCase = new DepositUseCase(accountRepository, accountEventRepository);

    @Test
    void deposits_funds_and_emits_deposited_event() {
        UUID accountId = UUID.randomUUID();
        Account account = new Account(accountId, "user-1", BigDecimal.valueOf(100), 0L, Instant.now());
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.updateBalanceWithVersion(accountId, BigDecimal.valueOf(150), 0L)).thenReturn(true);

        AccountEvent event = useCase.deposit(accountId, BigDecimal.valueOf(50));

        assertThat(event.eventType()).isEqualTo(AccountEventType.DEPOSITED);
        assertThat(event.balanceBefore()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(event.balanceAfter()).isEqualByComparingTo(BigDecimal.valueOf(150));

        ArgumentCaptor<AccountEvent> captor = ArgumentCaptor.forClass(AccountEvent.class);
        verify(accountEventRepository).save(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo(AccountEventType.DEPOSITED);
    }
}
