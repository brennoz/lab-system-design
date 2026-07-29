package com.interview.ood.q035_twitter_service_layer.solution;

// Design decision: one interface, two methods, not two segregated single-method interfaces
// (FollowObserver / LikeObserver). NotificationService is the only observer in this system and
// it always needs both events — segregating them would mean SocialGraph and LikeService each
// maintain their own observer list (they do anyway, since they're separate Subjects) AND
// callers would register the same NotificationService instance twice against two different
// interface types, for no behavioral gain. Revisit only if a second observer type appears that
// genuinely needs just one event.
// What NOT to do: don't collapse this into a single onEvent(Object event) method — that
// reintroduces exactly the instanceof/type-switch this question's constraints forbid, since
// every observer would have to branch on the event's runtime type to know what happened.
public interface TwitterEventObserver {
    void onFollowed(Follow follow);
    void onLiked(Like like);
}
