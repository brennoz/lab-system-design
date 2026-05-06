package com.lab.bank.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface SpringDataOutboxRepository extends JpaRepository<OutboxJpaEntity, UUID> {
    List<OutboxJpaEntity> findByPublishedFalse();

    @Modifying
    @Transactional
    @Query("UPDATE OutboxJpaEntity o SET o.published = true WHERE o.id = :id")
    void markPublished(@Param("id") UUID id);
}
