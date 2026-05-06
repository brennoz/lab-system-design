package com.lab.bank.application.usecase;

import com.lab.bank.domain.model.Account;
import com.lab.bank.domain.model.AccountEvent;
import com.lab.bank.domain.model.AccountNotFoundException;
import com.lab.bank.domain.port.AccountEventRepository;
import com.lab.bank.domain.port.AccountRepository;

import java.util.List;
import java.util.UUID;

public class GetAccountUseCase {

    private final AccountRepository accountRepository;
    private final AccountEventRepository accountEventRepository;

    public GetAccountUseCase(AccountRepository accountRepository,
                             AccountEventRepository accountEventRepository) {
        this.accountRepository = accountRepository;
        this.accountEventRepository = accountEventRepository;
    }

    public Account getAccount(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }

    public List<AccountEvent> getEvents(UUID accountId) {
        return accountEventRepository.findByAccountId(accountId);
    }
}
