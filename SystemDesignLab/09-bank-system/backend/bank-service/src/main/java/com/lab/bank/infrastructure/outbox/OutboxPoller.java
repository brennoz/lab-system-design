package com.lab.bank.infrastructure.outbox;

import com.lab.bank.domain.model.OutboxEvent;
import com.lab.bank.domain.port.OutboxPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;

// Pattern: Outbox — kafka.send().get() awaits broker ack before markPublished; retry on next tick if send fails
public class OutboxPoller {

    private static final Logger log = LoggerFactory.getLogger(OutboxPoller.class);
    private static final String TOPIC = "bank.events";

    private final OutboxPort outboxPort;
    private final KafkaTemplate<String, String> kafka;

    public OutboxPoller(OutboxPort outboxPort, KafkaTemplate<String, String> kafka) {
        this.outboxPort = outboxPort;
        this.kafka = kafka;
    }

    @Scheduled(fixedDelay = 1000)
    public void poll() {
        for (OutboxEvent event : outboxPort.findUnpublished()) {
            try {
                kafka.send(TOPIC, event.payload()).get();
                outboxPort.markPublished(event.id());
            } catch (Exception e) {
                log.warn("Outbox publish failed for event {}, will retry: {}", event.id(), e.getMessage());
            }
        }
    }
}
