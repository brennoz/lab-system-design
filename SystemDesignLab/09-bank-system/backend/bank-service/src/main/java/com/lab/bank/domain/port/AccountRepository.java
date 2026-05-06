package com.lab.bank.domain.port;

import com.lab.bank.domain.model.Account;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    void save(Account account);
    Optional<Account> findById(UUID id);
    // Returns false if version mismatch (concurrent modification); increments version on success
    boolean updateBalanceWithVersion(UUID id, BigDecimal newBalance, long expectedVersion);
}
