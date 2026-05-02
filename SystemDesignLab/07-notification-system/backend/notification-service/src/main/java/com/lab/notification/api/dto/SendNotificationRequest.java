package com.lab.notification.api.dto;

import com.lab.notification.domain.model.Channel;
import com.lab.notification.domain.model.Priority;

import java.util.List;

public record SendNotificationRequest(
        String type,
        List<String> recipients,
        Channel channel,
        Priority priority,
        String payload
) {}
