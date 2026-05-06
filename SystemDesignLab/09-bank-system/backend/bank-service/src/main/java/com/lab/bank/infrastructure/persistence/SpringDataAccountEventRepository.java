package com.lab.bank.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataAccountEventRepository extends JpaRepository<AccountEventJpaEntity, UUID> {
    List<AccountEventJpaEntity> findByAccountIdOrderByCreatedAtAsc(UUID accountId);
}
