package com.interview.ood.q035_twitter_service_layer.solution;

// Design decision: enum, not a String "type" field or a Notification subclass per event kind.
// An enum keeps Notification a single flat record (easy to store/query) while still letting a
// caller branch on it with a switch expression if needed — no instanceof, no type-switch on
// Java types, which this question's constraints explicitly forbid.
// What NOT to do: don't add a third case by subclassing Notification per type (e.g.
// FollowNotification, LikeNotification) — that fragments a currently-flat, easy-to-store
// record for no query benefit this question needs.
public enum NotificationType {
    FOLLOW,
    LIKE
}
