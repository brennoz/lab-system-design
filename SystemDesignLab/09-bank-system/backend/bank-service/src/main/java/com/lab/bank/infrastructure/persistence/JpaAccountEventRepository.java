package com.lab.bank.infrastructure.persistence;

import com.lab.bank.domain.model.AccountEvent;
import com.lab.bank.domain.port.AccountEventRepository;

import java.util.List;
import java.util.UUID;

public class JpaAccountEventRepository implements AccountEventRepository {

    private final SpringDataAccountEventRepository delegate;

    public JpaAccountEventRepository(SpringDataAccountEventRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public void save(AccountEvent event) {
        delegate.save(new AccountEventJpaEntity(event.id(), event.accountId(), event.eventType(),
                event.amount(), event.balanceBefore(), event.balanceAfter(),
                event.correlationId(), event.createdAt()));
    }

    @Override
    public List<AccountEvent> findByAccountId(UUID accountId) {
        return delegate.findByAccountIdOrderByCreatedAtAsc(accountId).stream()
                .map(e -> new AccountEvent(e.getId(), e.getAccountId(), e.getEventType(),
                        e.getAmount(), e.getBalanceBefore(), e.getBalanceAfter(),
                        e.getCorrelationId(), e.getCreatedAt()))
                .toList();
    }
}
