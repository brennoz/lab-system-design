package com.lab.bank.application;

import com.lab.bank.application.usecase.WithdrawUseCase;
import com.lab.bank.domain.model.Account;
import com.lab.bank.domain.model.AccountEvent;
import com.lab.bank.domain.model.AccountEventType;
import com.lab.bank.domain.model.InsufficientFundsException;
import com.lab.bank.domain.port.AccountEventRepository;
import com.lab.bank.domain.port.AccountRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class WithdrawUseCaseTest {

    private final AccountRepository accountRepository = mock(AccountRepository.class);
    private final AccountEventRepository accountEventRepository = mock(AccountEventRepository.class);
    private final WithdrawUseCase useCase = new WithdrawUseCase(accountRepository, accountEventRepository);

    @Test
    void withdraws_funds_and_emits_withdrawn_event() {
        UUID accountId = UUID.randomUUID();
        Account account = new Account(accountId, "user-1", BigDecimal.valueOf(200), 1L, Instant.now());
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.updateBalanceWithVersion(accountId, BigDecimal.valueOf(150), 1L)).thenReturn(true);

        AccountEvent event = useCase.withdraw(accountId, BigDecimal.valueOf(50));

        assertThat(event.eventType()).isEqualTo(AccountEventType.WITHDRAWN);
        assertThat(event.balanceBefore()).isEqualByComparingTo(BigDecimal.valueOf(200));
        assertThat(event.balanceAfter()).isEqualByComparingTo(BigDecimal.valueOf(150));
        verify(accountEventRepository).save(any());
    }

    @Test
    void throws_when_balance_insufficient() {
        UUID accountId = UUID.randomUUID();
        Account account = new Account(accountId, "user-1", BigDecimal.valueOf(10), 0L, Instant.now());
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> useCase.withdraw(accountId, BigDecimal.valueOf(50)))
                .isInstanceOf(InsufficientFundsException.class);

        verify(accountRepository, never()).updateBalanceWithVersion(any(), any(), anyLong());
    }
}
