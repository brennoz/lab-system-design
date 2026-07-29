# q035 Twitter Service Layer — Migrate to solution/answer + add Observer notifications

**Date:** 2026-07-29
**Project:** PassTheSoftwareEngineeringInterview
**Status:** Approved

## Context

`ood/.../q035_twitter_service_layer/` currently uses the old flat layout
(`MyAnswer.java` / `MyAnswerTest.java`), predating the `solution/` + `answer/`
folder convention introduced at q044 (CLAUDE.md "OOD Delivery Convention").
The existing `MyAnswer.java` is already complete and passing (see `review.md`
history — 6 bugs found and fixed).

Goal: rewrite q035 in the current `solution/`/`answer/` layout, and extend the
design to demonstrate the Observer pattern for a notification feature —
layered on top of the existing pull-model service layer, not replacing it.

Old flat files (`MyAnswer.java`, `MyAnswerTest.java`, `review.md`) are deleted;
`solution/` becomes the canonical reference, `answer/` ships empty.

## Architecture

Existing Service Layer pieces unchanged in behavior:
`User`, `Tweet`, `Follow`, `Like` (thin records) · `UserRepository` ·
`TweetRepository` · `SocialGraph` · `FeedService` · `LikeService` ·
`TwitterService` (facade). Pull-model timeline (`FeedService.getTimeline`)
stays query-based — no change.

New: Observer pattern for notifications, layered on top.

```
SocialGraph ──notifyObservers──┐
LikeService ──notifyObservers──┼──► TwitterEventObserver (interface)
                                │         └── NotificationService (concrete observer)
```

### `TwitterEventObserver` (new interface)

One interface, two methods (not split per-event — see rejected alternative below):

```java
interface TwitterEventObserver {
    void onFollowed(Follow follow);
    void onLiked(Like like);
}
```

Rejected alternative: two segregated interfaces (`FollowObserver`,
`LikeObserver`). More ISP-compliant but doubles the observer-list bookkeeping
in `SocialGraph`/`LikeService` for a subscriber (`NotificationService`) that
always needs both events. Revisit if a second observer type ever needs only
one event.

### Subjects — `SocialGraph` and `LikeService`

Both become Subjects directly (textbook Observer, matches q032's
`User.notifyObservers()` shape):
- `List<TwitterEventObserver> observers` field
- `addObserver(TwitterEventObserver)`
- `notifyObservers(...)` called at the exact point state mutates
  (`SocialGraph.follow()` after both adjacency maps update;
  `LikeService.like()` after both like maps update)

Rejected alternative: centralize publishing in `TwitterService` (facade calls
`notificationService.onFollowed(...)` manually after delegating). Simpler
wiring but isn't really Observer — just two sequential facade calls — and
wouldn't demonstrate the pattern the interview question tests.

### `Notification` (new record)

`(id, recipientId, actorId, type, message, createdAt)`.
`type: NotificationType` enum `{FOLLOW, LIKE}` — enum dispatch, not
`instanceof`/type-switch (respects the existing q035 constraint).

### `NotificationService implements TwitterEventObserver` (new)

Concrete observer, constructed with `UserRepository` + `TweetRepository`
(needed to resolve human-readable messages and the like's recipient):

- `onFollowed(Follow f)`: recipient = `f.followeeId()`; message built via
  `UserRepository` lookup of `f.followerId()`'s username, e.g.
  `"alice followed you"`.
- `onLiked(Like l)`: resolves the liked tweet via
  `TweetRepository.findById(l.tweetId())` to get the author as recipient.
  **Skips notifying when actor == recipient** (liking your own tweet doesn't
  notify you — deliberate edge case, matches real product behavior, called
  out explicitly in code comments).
- Storage: `Map<String, List<Notification>> byRecipient`.
- `getNotifications(userId)` → `List<Notification>`, newest first.
- Notifications are an **append-only event log** — `unfollow()`/`unlike()` do
  NOT retroactively remove past notifications. Worth a comment: this mirrors
  why `Follow`/`Like` are explicit entities rather than mutable state in the
  first place.

### Wiring in `TwitterService`

Constructor builds `NotificationService(userRepository, tweetRepository)`,
then subscribes it once: `socialGraph.addObserver(notificationService)`,
`likeService.addObserver(notificationService)`. Facade does not publish
events itself — only wires the subscription.

## File layout

Mirrors q044 exactly, under both `src/main` and `src/test`:

```
ood/src/main/java/.../q035_twitter_service_layer/
  question.md                 (rewritten: adds Observer/Notification requirements)
  solution/
    User.java, Tweet.java, Follow.java, Like.java
    Notification.java, NotificationType.java
    UserRepository.java, TweetRepository.java
    SocialGraph.java, FeedService.java, LikeService.java
    TwitterEventObserver.java, NotificationService.java
    TwitterService.java
    TwitterMain.java          (runnable demo — bonus, matches q044's AtmMain precedent)
  answer/
    .gitkeep

ood/src/test/java/.../q035_twitter_service_layer/
  solution/
    SolutionTest.java         (reference suite, TDD-narrative comments)
  answer/
    .gitkeep
```

Old flat `MyAnswer.java`, `MyAnswerTest.java`, `review.md` are deleted.

## Comment density

Per CLAUDE.md's OOD Delivery Convention, every `solution/` class states:
1. The design decision + why it fits this requirement.
2. An alternative considered and rejected, with concrete reason (see rejected
   alternatives above — these get attached to the classes they affect).
3. What NOT to do / the bug shape this design avoids (e.g. "don't let
   `NotificationService` push directly into `FeedService`'s timeline — that
   resurrects the observer-based feed q032 already showed doesn't scale for
   arbitrary historical queries; notifications and timeline are separate
   concerns").

Test file gets the same treatment — why each test is shaped as it is, and
what a superficially-similar-but-wrong version would miss.

## Test plan (`SolutionTest.java`)

Core behavior (unchanged from original q035, ports over):
1. Follower sees followee's tweet in timeline
2. Timeline empty when following nobody
3. Social graph queryable in both directions (`isFollowing`, `getFollowers`,
   `getFollowing`, `getFollowerCount`)
4. Like count increments; duplicate like throws `IllegalStateException`

New — Observer/notification behavior:
5. Follow triggers a notification for the followee
6. Like triggers a notification for the tweet's author
7. Liking your own tweet does NOT self-notify
8. Unfollow/unlike do NOT retroactively remove past notifications (event-log
   semantics)

~8 tests total in one `SolutionTest.java` (grouped, per q044 precedent —
not split one-class-per-file).

## Out of scope

- No change to `FeedService`'s pull-model timeline algorithm.
- No retrofit of other pre-q042 OOD questions (q035 is the only one being
  migrated here, per explicit user request).
- `answer/` stays empty (`.gitkeep`) — not pre-seeded with planted bugs
  (`next ood skeleton` mode not requested here).
