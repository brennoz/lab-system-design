package com.lab.flight.domain.port;

import com.lab.flight.domain.model.OutboxEvent;

import java.util.List;
import java.util.UUID;

// Port — Outbox pattern; save() called in same TX as booking; poller calls findUnpublished()
public interface OutboxPort {
    void save(OutboxEvent event);
    List<OutboxEvent> findUnpublished();
    void markPublished(UUID id);
}
