package com.lab.notification.application.usecase;

import com.lab.notification.domain.model.*;
import com.lab.notification.domain.port.*;
import com.lab.notification.domain.service.FanOutService;

import java.util.List;

// Pattern: CQRS write side — validates preferences, fans out tasks, enqueues by priority
public class SendNotificationUseCase {

    private final NotificationRepository repository;
    private final TaskQueuePort taskQueue;
    private final PreferencePort preferences;
    private final FanOutService fanOut;

    public SendNotificationUseCase(NotificationRepository repository, TaskQueuePort taskQueue,
                                   PreferencePort preferences, FanOutService fanOut) {
        this.repository = repository;
        this.taskQueue = taskQueue;
        this.preferences = preferences;
        this.fanOut = fanOut;
    }

    public void send(String type, List<String> recipients, Channel channel,
                     Priority priority, String payload) {
        List<String> active = recipients.stream()
                .filter(r -> !preferences.isOptedOut(r, channel))
                .toList();

        List<NotificationTask> tasks = fanOut.fanOut(type, active, channel, priority, payload);

        for (NotificationTask task : tasks) {
            // pendingWithId: notification row shares UUID with task so existsSent check is consistent
            Notification notification = Notification.pendingWithId(
                    task.notificationId(), type, task.recipientId(), channel, priority, payload);
            repository.save(notification);
            taskQueue.enqueue(task, priority);
        }
    }
}
