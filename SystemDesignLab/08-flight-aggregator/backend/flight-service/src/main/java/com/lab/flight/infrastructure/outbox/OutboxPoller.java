package com.lab.flight.infrastructure.outbox;

import com.lab.flight.domain.model.OutboxEvent;
import com.lab.flight.domain.port.OutboxPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

// Pattern: Outbox Poller — markPublished only AFTER confirmed broker ack; guarantees at-least-once
@Component
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);
    private static final String TOPIC = "flight.booking.events";

    private final OutboxPort outboxPort;
    private final KafkaTemplate<String, String> kafka;

    public OutboxPoller(OutboxPort outboxPort, KafkaTemplate<String, String> kafka) {
        this.outboxPort = outboxPort;
        this.kafka = kafka;
    }

    @Scheduled(fixedDelay = 1000)
    public void poll() {
        List<OutboxEvent> unpublished = outboxPort.findUnpublished();
        for (OutboxEvent event : unpublished) {
            try {
                // .get() blocks until broker acks — only then mark published to prevent silent loss
                kafka.send(TOPIC, event.payload()).get();
                outboxPort.markPublished(event.id());
            } catch (Exception e) {
                log.warn("Failed to publish outbox event {}: {}", event.id(), e.getMessage());
                // leave published=false; will retry on next poll tick
            }
        }
    }
}
