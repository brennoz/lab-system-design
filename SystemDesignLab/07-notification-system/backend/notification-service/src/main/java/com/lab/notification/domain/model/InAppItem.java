package com.lab.notification.domain.model;

import java.time.Instant;
import java.util.UUID;

// Value Object — projection of an in-app notification for inbox queries
public record InAppItem(UUID id, String payload, Instant createdAt) {}
