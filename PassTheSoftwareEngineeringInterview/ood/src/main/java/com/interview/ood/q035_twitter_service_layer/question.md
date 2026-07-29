**Lessons:**
- [0001 Tweet Model & Observer (q032 approach)](../../../../../topics/ood/twitter/0001-tweet-model-observer.html)
- [0002 Service Layer vs Rich Domain Model](../../../../../topics/ood/twitter/0002-twitter-service-layer.html)
- [0003 Observer-Driven Notifications (Full Solution Walkthrough)](../../../../../topics/ood/twitter/0003-observer-notifications-full-walkthrough.html)

# q035 — Twitter: Service Layer + Explicit Entities + Observer Notifications

**Category:** OOD · **Difficulty:** Medium · **Ideal time:** 30 min

---

## Design approach

This is a **re-implementation of Twitter** using the Service Layer pattern, extended with an
**Observer pattern notification feature**:

- `User`, `Tweet`, `Follow`, `Like`, `Notification` are **thin data records** — no behavior,
  no graph logic
- `SocialGraph` service owns follow/unfollow + all graph queries
- `TweetRepository` is an in-memory Map simulating persistence
- `FeedService` builds timelines by reading from `SocialGraph` + `TweetRepository`
  (pull-model — unchanged from the original q035)
- `LikeService` manages the Like relationship
- `SocialGraph` and `LikeService` are **Observer Subjects** — each publishes an event
  (`Follow`/`Like`) to registered `TwitterEventObserver`s the moment their state changes
- `NotificationService` is the concrete **Observer**, building a per-user notification inbox
  from both event streams
- `TwitterService` is a facade coordinating all services, and wires the Observer subscription
  once at construction

Compare your implementation's Observer usage to q032's `FeedObserver` (push-based *timeline*)
— here Observer drives *notifications*, a separate concern from the pull-based timeline.

---

## Requirements

1. `User`, `Tweet` are Java records (immutable value objects)
2. `Follow(followerId, followeeId, followedAt)` — explicit entity, created by
   `SocialGraph.follow()`, and also the event payload passed to observers
3. `Like(userId, tweetId, likedAt)` — explicit entity, created by `LikeService.like()`, and
   also the event payload passed to observers
4. `SocialGraph`:
   - `follow(followerId, followeeId)` → `Follow` (throws if self-follow), notifies observers
     after both adjacency maps update
   - `unfollow(followerId, followeeId)` — does **not** notify observers or retract past
     notifications
   - `getFollowing(userId)` → `Set<String>` (IDs of users this user follows)
   - `getFollowers(userId)` → `Set<String>` (IDs of users who follow this user)
   - `isFollowing(followerId, followeeId)` → `boolean`
   - `getFollowerCount(userId)` → `int`
   - `addObserver(TwitterEventObserver)`
5. `TweetRepository`:
   - `save(tweet)` — stores by id + secondary index by authorId
   - `findById(id)` → `Optional<Tweet>`
   - `findByAuthorId(authorId)` → `List<Tweet>`
6. `FeedService.getTimeline(userId, limit)`:
   - Get all followees from `SocialGraph`
   - Fetch their tweets from `TweetRepository`
   - Merge + sort descending by `createdAt` + take first `limit`
7. `LikeService`:
   - `like(userId, tweetId)` → `Like` (throws if already liked), notifies observers
   - `unlike(userId, tweetId)` (no-op if not liked) — does **not** notify observers
   - `getLikeCount(tweetId)` → `int`
   - `addObserver(TwitterEventObserver)`
8. `TwitterEventObserver` — one interface, two methods: `onFollowed(Follow)`, `onLiked(Like)`
9. `NotificationService implements TwitterEventObserver`:
   - `onFollowed(Follow)` → stores a `FOLLOW` notification for the followee
   - `onLiked(Like)` → stores a `LIKE` notification for the tweet's author, **except** when
     the liker is the tweet's own author (no self-notification)
   - `getNotifications(userId)` → `List<Notification>`, newest first
10. `TwitterService` (facade):
    - `register(username)` → `User`
    - `post(userId, content)` → `Tweet`
    - `follow(followerId, followeeId)` → `Follow`
    - `like(userId, tweetId)` → `Like`
    - `getTimeline(userId)` → `List<Tweet>`
    - Wires `notificationService` as an observer of both `socialGraph` and `likeService` in
      its constructor
    - Exposes `getSocialGraph()`, `getLikeService()`, `getNotificationService()` accessors

---

## Constraints

- No Spring, no JPA, pure Java 21
- Use `UUID.randomUUID().toString()` for IDs
- `Tweet` content max 280 chars — validate in `TwitterService.post()`
- No `instanceof` or type-switch anywhere (use the `NotificationType` enum for notification
  kind, not a type hierarchy)
- `SocialGraph` and `LikeService` publish events themselves (Subject role) — `TwitterService`
  only registers the subscription, it does not call `NotificationService` methods directly

---

## Folder layout

(Standard for `next ood` — see CLAUDE.md's "OOD Delivery Convention" section: full
solution-walkthrough lesson + complete `solution/` (including a runnable `TwitterMain.java`) +
empty `answer/`, mirrored under both `src/main` and `src/test`.)

```
q035_twitter_service_layer/
├── question.md
├── solution/   ← full reference implementation, one class per file, includes TwitterMain.java
└── answer/     ← empty (.gitkeep) — write from scratch, or study the lesson + solution/ first
```

---

## Hints

If stuck → type `hint`
To see full solution → type `solution`
To submit → type `done`
