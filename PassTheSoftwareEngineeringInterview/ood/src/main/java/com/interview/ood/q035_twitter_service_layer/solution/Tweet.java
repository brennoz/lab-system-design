package com.interview.ood.q035_twitter_service_layer.solution;

import java.time.Instant;

// Design decision: single flat record, not a Tweet/OriginalTweet/Retweet/Reply hierarchy
// (contrast q032). This question's scope is the service layer + social graph, not tweet
// polymorphism — keeping Tweet flat avoids an unrelated inheritance decision competing for
// attention with the pattern actually being taught here (Observer + Service Layer).
// What NOT to do: don't smuggle retweet/reply fields onto this record "just in case" — YAGNI;
// q032 already covers that variation.
public record Tweet(String id, String authorId, String content, String mediaUrl, Instant createdAt) {}
