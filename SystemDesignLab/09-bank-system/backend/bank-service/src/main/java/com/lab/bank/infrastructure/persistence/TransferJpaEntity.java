package com.lab.bank.infrastructure.persistence;

import com.lab.bank.domain.model.TransferStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfers")
public class TransferJpaEntity {

    @Id
    private UUID id;
    @Column(name = "from_account_id", nullable = false)
    private UUID fromAccountId;
    @Column(name = "to_account_id", nullable = false)
    private UUID toAccountId;
    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status;
    @Column(name = "idempotency_key", unique = true, nullable = false)
    private String idempotencyKey;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected TransferJpaEntity() {}

    public TransferJpaEntity(UUID id, UUID fromAccountId, UUID toAccountId, BigDecimal amount,
                              TransferStatus status, String idempotencyKey, Instant createdAt) {
        this.id = id; this.fromAccountId = fromAccountId; this.toAccountId = toAccountId;
        this.amount = amount; this.status = status; this.idempotencyKey = idempotencyKey;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getFromAccountId() { return fromAccountId; }
    public UUID getToAccountId() { return toAccountId; }
    public BigDecimal getAmount() { return amount; }
    public TransferStatus getStatus() { return status; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public Instant getCreatedAt() { return createdAt; }
    public void setStatus(TransferStatus status) { this.status = status; }
}
