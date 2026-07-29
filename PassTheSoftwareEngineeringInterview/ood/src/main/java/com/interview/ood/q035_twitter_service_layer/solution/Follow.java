package com.interview.ood.q035_twitter_service_layer.solution;

import java.time.Instant;

// Design decision: Follow is an explicit entity (a graph edge you can hand to a caller,
// serialize, or pass to an Observer), not just a side effect of mutating a Set inside User.
// This is also the event payload SocialGraph hands to TwitterEventObserver.onFollowed() —
// the same record serves as both "the return value of follow()" and "the notification event,"
// so there's no separate FollowEvent type duplicating the same three fields.
// What NOT to do: don't let SocialGraph call notificationService.onFollowed() directly
// instead of iterating its own observer list — that bypasses the Observer contract and
// silently breaks if a second observer is ever added.
public record Follow(String followerId, String followeeId, Instant followedAt) {}
