package com.lab.notification.infrastructure.channel;

import com.lab.notification.domain.model.Channel;
import com.lab.notification.domain.model.NotificationTask;
import com.lab.notification.domain.port.ChannelPort;
import com.lab.notification.infrastructure.inapp.InAppNotificationJpaEntity;
import com.lab.notification.infrastructure.inapp.SpringDataInAppRepository;

import java.time.Instant;

// Pattern: Strategy adapter — persists in-app notification directly to PostgreSQL; frontend polls inbox
public class InAppChannelAdapter implements ChannelPort {

    private final SpringDataInAppRepository repository;

    public InAppChannelAdapter(SpringDataInAppRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean supports(Channel channel) {
        return channel == Channel.IN_APP;
    }

    @Override
    public void send(NotificationTask task) {
        // existsById guard: idempotent on redelivery — duplicate key would throw otherwise
        if (!repository.existsById(task.notificationId())) {
            repository.save(new InAppNotificationJpaEntity(
                    task.notificationId(),
                    task.recipientId(),
                    task.payload(),
                    Instant.now()
            ));
        }
    }
}
