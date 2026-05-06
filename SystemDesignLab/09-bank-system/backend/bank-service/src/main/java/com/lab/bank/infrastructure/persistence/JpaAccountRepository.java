package com.lab.bank.infrastructure.persistence;

import com.lab.bank.domain.model.Account;
import com.lab.bank.domain.port.AccountRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class JpaAccountRepository implements AccountRepository {

    private final SpringDataAccountRepository delegate;

    public JpaAccountRepository(SpringDataAccountRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public void save(Account account) {
        delegate.save(new AccountJpaEntity(account.id(), account.ownerId(),
                account.balance(), account.version(), account.createdAt()));
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return delegate.findById(id).map(e ->
                new Account(e.getId(), e.getOwnerId(), e.getBalance(), e.getVersion(), e.getCreatedAt()));
    }

    @Override
    public boolean updateBalanceWithVersion(UUID id, BigDecimal newBalance, long expectedVersion) {
        return delegate.updateBalanceWithVersion(id, newBalance, expectedVersion) > 0;
    }
}
