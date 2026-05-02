package com.lab.notification.infrastructure.queue;

import com.lab.notification.application.usecase.ProcessNotificationTaskUseCase;
import com.lab.notification.domain.model.Channel;
import com.lab.notification.domain.model.NotificationTask;
import com.lab.notification.domain.model.Priority;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

// Pattern: Message consumer — decodes pipe-delimited message, delegates to ProcessNotificationTaskUseCase
@Component
public class RabbitMqConsumer {

    private final ProcessNotificationTaskUseCase processUseCase;

    public RabbitMqConsumer(ProcessNotificationTaskUseCase processUseCase) {
        this.processUseCase = processUseCase;
    }

    @RabbitListener(queues = {RabbitMqTaskQueue.CRITICAL_QUEUE, RabbitMqTaskQueue.BULK_QUEUE})
    public void onMessage(String message) {
        String[] parts = message.split("\\|", 5);
        NotificationTask task = new NotificationTask(
                UUID.fromString(parts[0]),
                parts[1],
                Channel.valueOf(parts[2]),
                Priority.valueOf(parts[3]),
                parts[4]);
        processUseCase.process(task);
    }
}
