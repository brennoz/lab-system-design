package com.lab.bank.infrastructure.persistence;

import com.lab.bank.domain.model.Transfer;
import com.lab.bank.domain.model.TransferStatus;
import com.lab.bank.domain.port.TransferRepository;

import java.util.Optional;
import java.util.UUID;

public class JpaTransferRepository implements TransferRepository {

    private final SpringDataTransferRepository delegate;

    public JpaTransferRepository(SpringDataTransferRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public void save(Transfer transfer) {
        delegate.save(new TransferJpaEntity(transfer.id(), transfer.fromAccountId(), transfer.toAccountId(),
                transfer.amount(), transfer.status(), transfer.idempotencyKey(), transfer.createdAt()));
    }

    @Override
    public Optional<Transfer> findById(UUID id) {
        return delegate.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Transfer> findByIdempotencyKey(String key) {
        return delegate.findByIdempotencyKey(key).map(this::toDomain);
    }

    @Override
    public void updateStatus(UUID id, TransferStatus status) {
        delegate.updateStatus(id, status);
    }

    private Transfer toDomain(TransferJpaEntity e) {
        return new Transfer(e.getId(), e.getFromAccountId(), e.getToAccountId(),
                e.getAmount(), e.getStatus(), e.getIdempotencyKey(), e.getCreatedAt());
    }
}
