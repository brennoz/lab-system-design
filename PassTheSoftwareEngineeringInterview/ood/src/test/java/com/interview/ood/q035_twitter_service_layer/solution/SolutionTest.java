package com.interview.ood.q035_twitter_service_layer.solution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolutionTest {

    private TwitterService twitter;

    @BeforeEach
    void setUp() {
        twitter = new TwitterService();
    }

    // What this verifies: FeedService's pull-model actually surfaces a followee's tweet — the
    // core promise of the "fan-out on read" algorithm. A weaker test might just check the
    // timeline isn't empty; asserting the specific tweet id is what actually proves the merge
    // pulled from the right followee rather than, say, every tweet in the repository.
    @Test
    void followerSeesFolloweeTweetInTimeline() {
        User alice = twitter.register("alice");
        User bob = twitter.register("bob");

        twitter.follow(bob.id(), alice.id());
        Tweet tweet = twitter.post(alice.id(), "Hello world!");

        List<Tweet> timeline = twitter.getTimeline(bob.id());
        assertEquals(1, timeline.size());
        assertEquals(tweet.id(), timeline.get(0).id());
    }

    @Test
    void timelineEmptyWhenNotFollowingAnyone() {
        User bob = twitter.register("bob");
        assertTrue(twitter.getTimeline(bob.id()).isEmpty());
    }

    // What this verifies: SocialGraph's two adjacency maps (following/followers) stay
    // consistent with each other after a single follow() call — this is the query capability
    // q032's push-based Observer model couldn't offer at all (no way to ask "who follows
    // alice" without walking every User's private observer list).
    @Test
    void socialGraphQueryableInBothDirections() {
        User alice = twitter.register("alice");
        User bob = twitter.register("bob");

        twitter.follow(bob.id(), alice.id());

        assertTrue(twitter.getSocialGraph().isFollowing(bob.id(), alice.id()));
        assertTrue(twitter.getSocialGraph().getFollowers(alice.id()).contains(bob.id()));
        assertTrue(twitter.getSocialGraph().getFollowing(bob.id()).contains(alice.id()));
        assertEquals(1, twitter.getSocialGraph().getFollowerCount(alice.id()));
    }

    @Test
    void likeCountIncrementsAndDuplicateLikeThrows() {
        User alice = twitter.register("alice");
        Tweet tweet = twitter.post(alice.id(), "Likeable tweet");

        twitter.like(alice.id(), tweet.id());
        assertEquals(1, twitter.getLikeService().getLikeCount(tweet.id()));

        assertThrows(IllegalStateException.class, () -> twitter.like(alice.id(), tweet.id()));
    }

    // What this verifies: SocialGraph, as a Subject, actually calls notifyObservers() inside
    // follow() — not that NotificationService's logic works in isolation (that's a separate
    // concern already covered by the message-content assertion below). A version of this test
    // that mocked TwitterEventObserver would only prove SocialGraph calls SOME observer method;
    // going through the real NotificationService proves the end-to-end wiring TwitterService's
    // constructor sets up.
    @Test
    void followTriggersNotificationForFollowee() {
        User alice = twitter.register("alice");
        User bob = twitter.register("bob");

        twitter.follow(bob.id(), alice.id());

        List<Notification> notifications = twitter.getNotificationService().getNotifications(alice.id());
        assertEquals(1, notifications.size());
        assertEquals(NotificationType.FOLLOW, notifications.get(0).type());
        assertEquals("bob followed you", notifications.get(0).message());
    }

    @Test
    void likeTriggersNotificationForTweetAuthor() {
        User alice = twitter.register("alice");
        User bob = twitter.register("bob");
        Tweet tweet = twitter.post(alice.id(), "Hello world!");

        twitter.like(bob.id(), tweet.id());

        List<Notification> notifications = twitter.getNotificationService().getNotifications(alice.id());
        assertEquals(1, notifications.size());
        assertEquals(NotificationType.LIKE, notifications.get(0).type());
        assertEquals("bob liked your tweet", notifications.get(0).message());
    }

    // What this verifies: the self-notification guard in NotificationService.onLiked() — the
    // deliberate edge case where recipientId equals the actor. Without this test, deleting the
    // "if (recipientId.equals(like.userId())) return;" guard would silently pass every other
    // test (they never like their own tweet), which is exactly the kind of regression a test
    // suite is supposed to catch.
    @Test
    void likingOwnTweetDoesNotSelfNotify() {
        User alice = twitter.register("alice");
        Tweet tweet = twitter.post(alice.id(), "My own tweet");

        twitter.like(alice.id(), tweet.id());

        assertTrue(twitter.getNotificationService().getNotifications(alice.id()).isEmpty());
    }

    // What this verifies: notifications are an append-only event log, not a live view derived
    // from current SocialGraph/LikeService state. A version of NotificationService that
    // re-queried "is bob still following alice?" when building getNotifications() would make
    // this test fail — which would be wrong, since "bob followed you" was true at the time it
    // happened and stays true as history even after bob unfollows.
    @Test
    void unfollowDoesNotRetroactivelyRemovePastNotification() {
        User alice = twitter.register("alice");
        User bob = twitter.register("bob");

        twitter.follow(bob.id(), alice.id());
        twitter.getSocialGraph().unfollow(bob.id(), alice.id());

        assertEquals(1, twitter.getNotificationService().getNotifications(alice.id()).size());
    }
}
