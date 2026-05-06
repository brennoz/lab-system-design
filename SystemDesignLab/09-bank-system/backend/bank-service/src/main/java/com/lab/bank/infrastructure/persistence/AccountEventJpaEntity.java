package com.lab.bank.infrastructure.persistence;

import com.lab.bank.domain.model.AccountEventType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "account_events")
public class AccountEventJpaEntity {

    @Id
    private UUID id;
    @Column(name = "account_id", nullable = false)
    private UUID accountId;
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private AccountEventType eventType;
    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;
    @Column(name = "balance_before", precision = 19, scale = 4, nullable = false)
    private BigDecimal balanceBefore;
    @Column(name = "balance_after", precision = 19, scale = 4, nullable = false)
    private BigDecimal balanceAfter;
    @Column(name = "correlation_id")
    private UUID correlationId;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AccountEventJpaEntity() {}

    public AccountEventJpaEntity(UUID id, UUID accountId, AccountEventType eventType,
                                  BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter,
                                  UUID correlationId, Instant createdAt) {
        this.id = id; this.accountId = accountId; this.eventType = eventType;
        this.amount = amount; this.balanceBefore = balanceBefore; this.balanceAfter = balanceAfter;
        this.correlationId = correlationId; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public UUID getAccountId() { return accountId; }
    public AccountEventType getEventType() { return eventType; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getBalanceBefore() { return balanceBefore; }
    public BigDecimal getBalanceAfter() { return balanceAfter; }
    public UUID getCorrelationId() { return correlationId; }
    public Instant getCreatedAt() { return createdAt; }
}
