package com.interview.ood.q035_twitter_service_layer.solution;

// Design decision: thin data record, no behavior — matches this project's "prod-leaning OOD"
// convention (see q033 onward): domain objects hold state, service classes own logic and
// queries. A User doesn't know how to follow anyone; SocialGraph does.
// Rejected alternative: a rich User.follow(User target) method (q032's original approach) —
// rejected here specifically because q035 exists to demonstrate the opposite tradeoff: an
// explicit SocialGraph makes "who follows X" and "who does X follow" O(1) queries instead of
// walking every User's private observer list.
public record User(String id, String username, String bio) {}
