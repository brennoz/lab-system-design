package com.lab.bank.domain.port;

import com.lab.bank.domain.model.AccountEvent;

import java.util.List;
import java.util.UUID;

public interface AccountEventRepository {
    void save(AccountEvent event);
    List<AccountEvent> findByAccountId(UUID accountId);
}
