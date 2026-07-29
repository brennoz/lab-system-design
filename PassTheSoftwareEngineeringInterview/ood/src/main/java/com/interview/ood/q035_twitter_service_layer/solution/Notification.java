package com.interview.ood.q035_twitter_service_layer.solution;

import java.time.Instant;

// Design decision: thin record, same as every other domain entity here. recipientId is who
// SEES this notification; actorId is who CAUSED it (the follower, the liker) — keeping both
// means a future UI can render "alice followed you" without a second lookup.
// What NOT to do: don't make this mutable with an isRead flag bolted on later without
// thinking it through — that's a real feature (read/unread state) this question doesn't ask
// for; adding it speculatively would be scope creep past what q035 tests.
public record Notification(String id, String recipientId, String actorId, NotificationType type,
                            String message, Instant createdAt) {}
