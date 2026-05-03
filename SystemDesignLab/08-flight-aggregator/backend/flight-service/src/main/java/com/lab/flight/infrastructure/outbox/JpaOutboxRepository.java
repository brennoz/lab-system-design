package com.lab.flight.infrastructure.outbox;

import com.lab.flight.domain.model.OutboxEvent;
import com.lab.flight.domain.port.OutboxPort;

import java.util.List;
import java.util.UUID;

// Pattern: Repository adapter — maps OutboxEvent record to JPA entity
public class JpaOutboxRepository implements OutboxPort {

    private final SpringDataOutboxRepository delegate;

    public JpaOutboxRepository(SpringDataOutboxRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public void save(OutboxEvent event) {
        delegate.save(new OutboxJpaEntity(event.id(), event.aggregateId(), event.eventType(),
                event.payload(), event.published(), event.createdAt()));
    }

    @Override
    public List<OutboxEvent> findUnpublished() {
        return delegate.findByPublishedFalse().stream()
                .map(e -> new OutboxEvent(e.getId(), e.getAggregateId(), e.getEventType(),
                        e.getPayload(), e.isPublished(), e.getCreatedAt()))
                .toList();
    }

    @Override
    public void markPublished(UUID id) {
        delegate.findById(id).ifPresent(e -> {
            e.setPublished(true);
            delegate.save(e);
        });
    }
}
