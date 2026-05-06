package com.lab.bank.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class AccountJpaEntity {

    @Id
    private UUID id;
    @Column(name = "owner_id", nullable = false)
    private String ownerId;
    @Column(precision = 19, scale = 4, nullable = false)
    private BigDecimal balance;
    @Column(nullable = false)
    private long version;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AccountJpaEntity() {}

    public AccountJpaEntity(UUID id, String ownerId, BigDecimal balance, long version, Instant createdAt) {
        this.id = id; this.ownerId = ownerId; this.balance = balance;
        this.version = version; this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getOwnerId() { return ownerId; }
    public BigDecimal getBalance() { return balance; }
    public long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
}
