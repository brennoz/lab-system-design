package com.lab.bank.application.usecase;

import com.lab.bank.domain.model.Account;
import com.lab.bank.domain.port.AccountRepository;

public class CreateAccountUseCase {

    private final AccountRepository accountRepository;

    public CreateAccountUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account create(String ownerId) {
        Account account = Account.open(ownerId);
        accountRepository.save(account);
        return account;
    }
}
