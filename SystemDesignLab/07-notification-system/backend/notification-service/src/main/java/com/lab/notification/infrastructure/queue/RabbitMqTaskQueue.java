package com.lab.notification.infrastructure.queue;

import com.lab.notification.domain.model.NotificationTask;
import com.lab.notification.domain.model.Priority;
import com.lab.notification.domain.port.TaskQueuePort;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

// Pattern: Work queue adapter — routes to critical or bulk queue based on priority
public class RabbitMqTaskQueue implements TaskQueuePort {

    public static final String CRITICAL_QUEUE = "notifications.critical";
    public static final String BULK_QUEUE     = "notifications.bulk";

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqTaskQueue(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void enqueue(NotificationTask task, Priority priority) {
        String queue = priority == Priority.CRITICAL ? CRITICAL_QUEUE : BULK_QUEUE;
        // pipe-delimited: notificationId|recipientId|channel|payload
        String message = task.notificationId() + "|" + task.recipientId() + "|"
                + task.channel() + "|" + task.priority() + "|" + task.payload();
        rabbitTemplate.convertAndSend(queue, message);
    }
}
