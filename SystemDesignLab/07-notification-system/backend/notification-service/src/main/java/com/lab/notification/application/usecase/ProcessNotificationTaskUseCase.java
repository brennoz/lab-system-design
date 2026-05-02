package com.lab.notification.application.usecase;

import com.lab.notification.domain.model.NotificationStatus;
import com.lab.notification.domain.model.NotificationTask;
import com.lab.notification.domain.port.ChannelPort;
import com.lab.notification.domain.port.NotificationRepository;

import java.util.List;

// Pattern: Command — idempotency gate then strategy dispatch; safe on RabbitMQ at-least-once
public class ProcessNotificationTaskUseCase {

    private final NotificationRepository repository;
    private final List<ChannelPort> channelPorts;

    public ProcessNotificationTaskUseCase(NotificationRepository repository, List<ChannelPort> channelPorts) {
        this.repository = repository;
        this.channelPorts = channelPorts;
    }

    public void process(NotificationTask task) {
        if (repository.existsSent(task.notificationId())) {
            return;
        }
        ChannelPort port = channelPorts.stream()
                .filter(p -> p.supports(task.channel()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No channel adapter for " + task.channel()));
        try {
            port.send(task);
            repository.updateStatus(task.notificationId(), NotificationStatus.SENT);
        } catch (Exception e) {
            // Write FAILED so the row doesn't stay PENDING forever; re-throw for RabbitMQ DLQ routing
            repository.updateStatus(task.notificationId(), NotificationStatus.FAILED);
            throw e;
        }
    }
}
