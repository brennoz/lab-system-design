package com.lab.notification.domain.service;

import com.lab.notification.domain.model.*;

import java.util.List;
import java.util.UUID;

// Pattern: Domain Service — stateless; creates one NotificationTask per recipient
public class FanOutService {

    public List<NotificationTask> fanOut(String type, List<String> recipients,
                                         Channel channel, Priority priority, String payload) {
        return recipients.stream()
                .map(recipientId -> new NotificationTask(UUID.randomUUID(), recipientId, channel, priority, payload))
                .toList();
    }
}
