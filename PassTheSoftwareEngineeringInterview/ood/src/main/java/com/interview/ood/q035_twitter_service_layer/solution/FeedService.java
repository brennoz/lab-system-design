package com.interview.ood.q035_twitter_service_layer.solution;

import java.util.Comparator;
import java.util.List;

// Algorithm: fan-out on read — pull every followee's tweets at query time, merge, sort, and
// take the top N. O(F * T) per call (F = followees, T = avg tweets per followee), no
// write-time cost. Contrast q032's fan-out-on-write Observer model: O(1) at read time but
// O(followers) at write time, and — the actual limitation this question exists to show —
// no way to ask "what did I miss since yesterday" without replaying every push.
// What NOT to do: don't cache the merged timeline and have NotificationService or SocialGraph
// mutate it in place on every event — that would silently reintroduce the push-based staleness
// problem this design deliberately avoids by staying pull-only.
public class FeedService {

    private final SocialGraph socialGraph;
    private final TweetRepository tweetRepository;

    public FeedService(SocialGraph socialGraph, TweetRepository tweetRepository) {
        this.socialGraph = socialGraph;
        this.tweetRepository = tweetRepository;
    }

    public List<Tweet> getTimeline(String userId, int limit) {
        return socialGraph.getFollowing(userId).stream()
                .flatMap(followeeId -> tweetRepository.findByAuthorId(followeeId).stream())
                .sorted(Comparator.comparing(Tweet::createdAt).reversed())
                .limit(limit)
                .toList();
    }
}
