package com.lab.bank.application.usecase;

import com.lab.bank.domain.model.Account;
import com.lab.bank.domain.model.AccountEvent;
import com.lab.bank.domain.model.AccountEventType;
import com.lab.bank.domain.model.AccountNotFoundException;
import com.lab.bank.domain.model.OptimisticLockException;
import com.lab.bank.domain.port.AccountEventRepository;
import com.lab.bank.domain.port.AccountRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

public class WithdrawUseCase {

    private final AccountRepository accountRepository;
    private final AccountEventRepository accountEventRepository;

    public WithdrawUseCase(AccountRepository accountRepository,
                           AccountEventRepository accountEventRepository) {
        this.accountRepository = accountRepository;
        this.accountEventRepository = accountEventRepository;
    }

    @Transactional
    public AccountEvent withdraw(UUID accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        Account debited = account.debit(amount); // throws InsufficientFundsException if balance < amount
        boolean ok = accountRepository.updateBalanceWithVersion(accountId, debited.balance(), account.version());
        if (!ok) throw new OptimisticLockException(accountId);
        AccountEvent event = AccountEvent.of(accountId, AccountEventType.WITHDRAWN, amount,
                account.balance(), debited.balance(), accountId);
        accountEventRepository.save(event);
        return event;
    }
}
