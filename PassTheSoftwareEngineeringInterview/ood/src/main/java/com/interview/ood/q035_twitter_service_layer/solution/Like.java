package com.interview.ood.q035_twitter_service_layer.solution;

import java.time.Instant;

// Design decision: same reasoning as Follow — Like is the join entity AND the event payload
// passed to TwitterEventObserver.onLiked(). Reusing it means NotificationService.onLiked()
// resolves everything it needs (tweetId -> author lookup) from data LikeService already had,
// without LikeService needing to know anything about notifications.
// What NOT to do: same reasoning as Follow — don't let LikeService call NotificationService
// methods directly; iterate its own observer list instead, so adding a second observer later
// doesn't require touching LikeService.
public record Like(String userId, String tweetId, Instant likedAt) {}
