package com.lab.bank.domain.port;

import com.lab.bank.domain.model.OutboxEvent;

import java.util.List;
import java.util.UUID;

public interface OutboxPort {
    void save(OutboxEvent event);
    List<OutboxEvent> findUnpublished();
    void markPublished(UUID id);
}
