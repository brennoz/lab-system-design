package com.lab.notification.domain.port;

import com.lab.notification.domain.model.NotificationTask;
import com.lab.notification.domain.model.Priority;

// Pattern: Queue port — routes tasks to CRITICAL or BULK lane based on priority
public interface TaskQueuePort {
    void enqueue(NotificationTask task, Priority priority);
}
