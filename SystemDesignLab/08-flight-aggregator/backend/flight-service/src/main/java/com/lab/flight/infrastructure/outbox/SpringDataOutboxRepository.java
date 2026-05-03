package com.lab.flight.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataOutboxRepository extends JpaRepository<OutboxJpaEntity, UUID> {
    List<OutboxJpaEntity> findByPublishedFalse();
}
