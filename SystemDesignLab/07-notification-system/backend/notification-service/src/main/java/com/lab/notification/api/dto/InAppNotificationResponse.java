package com.lab.notification.api.dto;

import java.time.Instant;
import java.util.UUID;

public record InAppNotificationResponse(UUID id, String payload, Instant createdAt) {}
