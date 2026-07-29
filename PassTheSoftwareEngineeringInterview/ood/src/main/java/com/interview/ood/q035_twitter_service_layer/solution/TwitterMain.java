package com.interview.ood.q035_twitter_service_layer.solution;

// Runnable demo — not a test. Walks register -> follow -> post -> like -> notifications ->
// timeline, the same sequence SolutionTest exercises, printed for a human to read. Precedent:
// q044's AtmMain.java.
public class TwitterMain {

    public static void main(String[] args) {
        TwitterService twitter = new TwitterService();

        System.out.println("=== Register alice and bob ===");
        User alice = twitter.register("alice");
        User bob = twitter.register("bob");
        System.out.println("alice.id=" + alice.id() + " bob.id=" + bob.id());

        System.out.println();
        System.out.println("=== bob follows alice ===");
        twitter.follow(bob.id(), alice.id());
        System.out.println("alice's notifications: "
                + twitter.getNotificationService().getNotifications(alice.id()));

        System.out.println();
        System.out.println("=== alice posts a tweet ===");
        Tweet tweet = twitter.post(alice.id(), "Hello, Twitter!");
        System.out.println("Posted: " + tweet.content());

        System.out.println();
        System.out.println("=== bob's timeline (pull-model, includes alice's tweet) ===");
        System.out.println(twitter.getTimeline(bob.id()));

        System.out.println();
        System.out.println("=== bob likes alice's tweet ===");
        twitter.like(bob.id(), tweet.id());
        System.out.println("alice's notifications: "
                + twitter.getNotificationService().getNotifications(alice.id()));
        System.out.println("Like count: " + twitter.getLikeService().getLikeCount(tweet.id()));

        System.out.println();
        System.out.println("=== alice likes her own tweet (no self-notification) ===");
        twitter.like(alice.id(), tweet.id());
        System.out.println("alice's notifications (unchanged): "
                + twitter.getNotificationService().getNotifications(alice.id()));
    }
}
