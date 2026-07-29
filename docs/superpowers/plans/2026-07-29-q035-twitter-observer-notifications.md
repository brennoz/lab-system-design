# q035 Twitter Service Layer — solution/answer migration + Observer notifications Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate `ood` q035 (Twitter Service Layer) from its old flat `MyAnswer.java` layout to the `solution/`/`answer/` folder convention (matching q044), and add an Observer-pattern notification feature (follow/like events) layered on top of the existing pull-model timeline.

**Architecture:** `SocialGraph` and `LikeService` become Observer Subjects (each holds its own observer list, notifies at the point state mutates). `NotificationService` is the sole concrete Observer, subscribed to both from `TwitterService`'s constructor. All 8 reference tests exercise the system through the `TwitterService` facade (matching this project's existing test convention — see the current `MyAnswerTest.java`), so the full dependency chain (records → repositories → interface → services → facade) must compile before any test can run. Tasks 3–10 build that chain with complete, fully-commented implementations; Task 11 is the verification step — the test file is written fresh against already-implemented code and run once to confirm it encodes the exact behaviors specified in the design spec. Any failure at that point is a bug to fix immediately, not a design gap, since every method's behavior was already fully specified in Tasks 3–10.

**Tech Stack:** Java 21, JUnit 5, Maven (`ood` module). No Mockito needed — no external collaborators to mock; all Subjects/Observers are real objects wired through `TwitterService`.

---

## Reference: spec

Full design at `docs/superpowers/specs/2026-07-29-q035-twitter-observer-notifications-design.md`.

## Reference: existing conventions being followed

- Folder layout precedent: `ood/src/main/java/com/interview/ood/q044_atm_system/{solution,answer}/` and `ood/src/test/java/com/interview/ood/q044_atm_system/{solution,answer}/`.
- Comment density precedent: `ood/src/main/java/com/interview/ood/q044_atm_system/solution/AtmService.java`.
- Test comment precedent: `ood/src/test/java/com/interview/ood/q044_atm_system/solution/SolutionTest.java`.
- `BigDecimal`/Mockito rules don't apply here (no money, no external collaborators) but the project-wide rule stands: new tests use JUnit 5 assertions only, no AssertJ.

---

### Task 1: Remove old flat q035 files

**Files:**
- Delete: `ood/src/main/java/com/interview/ood/q035_twitter_service_layer/MyAnswer.java`
- Delete: `ood/src/main/java/com/interview/ood/q035_twitter_service_layer/review.md`
- Delete: `ood/src/test/java/com/interview/ood/q035_twitter_service_layer/MyAnswerTest.java`

- [ ] **Step 1: Delete the three files**

```bash
git rm ood/src/main/java/com/interview/ood/q035_twitter_service_layer/MyAnswer.java
git rm ood/src/main/java/com/interview/ood/q035_twitter_service_layer/review.md
git rm ood/src/test/java/com/interview/ood/q035_twitter_service_layer/MyAnswerTest.java
```

- [ ] **Step 2: Verify the module still builds without them**

Run: `mvn compile -pl ood`
Expected: `BUILD SUCCESS` (nothing else in the `ood` module references these classes — `q035_twitter_service_layer` package will be empty except `question.md` at this point).

- [ ] **Step 3: Commit**

```bash
git commit -m "chore: remove old flat q035 files ahead of solution/answer migration"
```

---

### Task 2: Rewrite question.md

**Files:**
- Modify: `ood/src/main/java/com/interview/ood/q035_twitter_service_layer/question.md`

- [ ] **Step 1: Replace the file contents**

```markdown
**Lessons:**
- [0001 Tweet Model & Observer (q032 approach)](../../../../../topics/ood/twitter/0001-tweet-model-observer.html)
- [0002 Service Layer vs Rich Domain Model](../../../../../topics/ood/twitter/0002-twitter-service-layer.html)

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
```

- [ ] **Step 2: Commit**

```bash
git add ood/src/main/java/com/interview/ood/q035_twitter_service_layer/question.md
git commit -m "docs: rewrite q035 question.md for Observer notification requirements"
```

---

### Task 3: Domain records + NotificationType enum

**Files:**
- Create: `ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/User.java`
- Create: `ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/Tweet.java`
- Create: `ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/Follow.java`
- Create: `ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/Like.java`
- Create: `ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/NotificationType.java`
- Create: `ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/Notification.java`

- [ ] **Step 1: Write `User.java`**

```java
package com.interview.ood.q035_twitter_service_layer.solution;

// Design decision: thin data record, no behavior — matches this project's "prod-leaning OOD"
// convention (see q033 onward): domain objects hold state, service classes own logic and
// queries. A User doesn't know how to follow anyone; SocialGraph does.
// Rejected alternative: a rich User.follow(User target) method (q032's original approach) —
// rejected here specifically because q035 exists to demonstrate the opposite tradeoff: an
// explicit SocialGraph makes "who follows X" and "who does X follow" O(1) queries instead of
// walking every User's private observer list.
public record User(String id, String username, String bio) {}
```

- [ ] **Step 2: Write `Tweet.java`**

```java
package com.interview.ood.q035_twitter_service_layer.solution;

import java.time.Instant;

// Design decision: single flat record, not a Tweet/OriginalTweet/Retweet/Reply hierarchy
// (contrast q032). This question's scope is the service layer + social graph, not tweet
// polymorphism — keeping Tweet flat avoids an unrelated inheritance decision competing for
// attention with the pattern actually being taught here (Observer + Service Layer).
// What NOT to do: don't smuggle retweet/reply fields onto this record "just in case" — YAGNI;
// q032 already covers that variation.
public record Tweet(String id, String authorId, String content, String mediaUrl, Instant createdAt) {}
```

- [ ] **Step 3: Write `Follow.java`**

```java
package com.interview.ood.q035_twitter_service_layer.solution;

import java.time.Instant;

// Design decision: Follow is an explicit entity (a graph edge you can hand to a caller,
// serialize, or pass to an Observer), not just a side effect of mutating a Set inside User.
// This is also the event payload SocialGraph hands to TwitterEventObserver.onFollowed() —
// the same record serves as both "the return value of follow()" and "the notification event,"
// so there's no separate FollowEvent type duplicating the same three fields.
public record Follow(String followerId, String followeeId, Instant followedAt) {}
```

- [ ] **Step 4: Write `Like.java`**

```java
package com.interview.ood.q035_twitter_service_layer.solution;

import java.time.Instant;

// Design decision: same reasoning as Follow — Like is the join entity AND the event payload
// passed to TwitterEventObserver.onLiked(). Reusing it means NotificationService.onLiked()
// resolves everything it needs (tweetId -> author lookup) from data LikeService already had,
// without LikeService needing to know anything about notifications.
public record Like(String userId, String tweetId, Instant likedAt) {}
```

- [ ] **Step 5: Write `NotificationType.java`**

```java
package com.interview.ood.q035_twitter_service_layer.solution;

// Design decision: enum, not a String "type" field or a Notification subclass per event kind.
// An enum keeps Notification a single flat record (easy to store/query) while still letting a
// caller branch on it with a switch expression if needed — no instanceof, no type-switch on
// Java types, which this question's constraints explicitly forbid.
public enum NotificationType {
    FOLLOW,
    LIKE
}
```

- [ ] **Step 6: Write `Notification.java`**

```java
package com.interview.ood.q035_twitter_service_layer.solution;

import java.time.Instant;

// Design decision: thin record, same as every other domain entity here. recipientId is who
// SEES this notification; actorId is who CAUSED it (the follower, the liker) — keeping both
// means a future UI can render "alice followed you" without a second lookup.
// What NOT to do: don't make this mutable with an isRead flag bolted on later without
// thinking it through — that's a real feature (read/unread state) this question doesn't ask
// for; adding it speculatively would be scope creep past what q035 tests.
public record Notification(String id, String recipientId, String actorId, NotificationType type,
                            String message, Instant createdAt) {}
```

- [ ] **Step 7: Verify it compiles**

Run: `mvn compile -pl ood`
Expected: `BUILD SUCCESS`

- [ ] **Step 8: Commit**

```bash
git add ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/User.java \
        ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/Tweet.java \
        ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/Follow.java \
        ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/Like.java \
        ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/NotificationType.java \
        ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/Notification.java
git commit -m "feat(q035): add domain records and NotificationType enum"
```

---

### Task 4: TwitterEventObserver interface

**Files:**
- Create: `ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/TwitterEventObserver.java`

- [ ] **Step 1: Write the file**

```java
package com.interview.ood.q035_twitter_service_layer.solution;

// Design decision: one interface, two methods, not two segregated single-method interfaces
// (FollowObserver / LikeObserver). NotificationService is the only observer in this system and
// it always needs both events — segregating them would mean SocialGraph and LikeService each
// maintain their own observer list (they do anyway, since they're separate Subjects) AND
// callers would register the same NotificationService instance twice against two different
// interface types, for no behavioral gain. Revisit only if a second observer type appears that
// genuinely needs just one event.
public interface TwitterEventObserver {
    void onFollowed(Follow follow);
    void onLiked(Like like);
}
```

- [ ] **Step 2: Verify it compiles**

Run: `mvn compile -pl ood`
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/TwitterEventObserver.java
git commit -m "feat(q035): add TwitterEventObserver interface"
```

---

### Task 5: Repositories

**Files:**
- Create: `ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/UserRepository.java`
- Create: `ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/TweetRepository.java`

- [ ] **Step 1: Write `UserRepository.java`**

```java
package com.interview.ood.q035_twitter_service_layer.solution;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Design decision: in-memory Map standing in for a real users table — this project's stated
// convention (see q033 onward) is Map-based repos with no ORM, since these questions test OOD
// reasoning, not persistence frameworks.
public class UserRepository {

    private final Map<String, User> store = new HashMap<>();

    public User save(User user) {
        store.put(user.id(), user);
        return user;
    }

    public Optional<User> findById(String userId) {
        return Optional.ofNullable(store.get(userId));
    }
}
```

- [ ] **Step 2: Write `TweetRepository.java`**

```java
package com.interview.ood.q035_twitter_service_layer.solution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Design decision: two maps — byId for direct lookup (NotificationService needs this to
// resolve a Like's tweet author) and byAuthor as a secondary index so FeedService doesn't
// scan every tweet ever posted to build one user's timeline.
// What NOT to do: don't rebuild byAuthor by filtering byId.values() on every read — that's
// the O(n) scan this secondary index exists specifically to avoid.
public class TweetRepository {

    private final Map<String, Tweet> byId = new HashMap<>();
    private final Map<String, List<Tweet>> byAuthor = new HashMap<>();

    public Tweet save(Tweet tweet) {
        byId.put(tweet.id(), tweet);
        byAuthor.computeIfAbsent(tweet.authorId(), k -> new ArrayList<>()).add(tweet);
        return tweet;
    }

    public Optional<Tweet> findById(String tweetId) {
        return Optional.ofNullable(byId.get(tweetId));
    }

    public List<Tweet> findByAuthorId(String authorId) {
        return byAuthor.getOrDefault(authorId, List.of());
    }
}
```

- [ ] **Step 3: Verify it compiles**

Run: `mvn compile -pl ood`
Expected: `BUILD SUCCESS`

- [ ] **Step 4: Commit**

```bash
git add ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/UserRepository.java \
        ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/TweetRepository.java
git commit -m "feat(q035): add UserRepository and TweetRepository"
```

---

### Task 6: SocialGraph (Observer Subject)

**Files:**
- Create: `ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/SocialGraph.java`

- [ ] **Step 1: Write the file**

```java
package com.interview.ood.q035_twitter_service_layer.solution;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Pattern: Observer (Subject side) — SocialGraph is the Subject for follow events, holding
// its own observer list and calling notifyObservers() at the exact point state changes
// (inside follow(), after both adjacency maps update). This is the textbook GoF shape: the
// object whose state changes publishes, rather than a facade manually re-announcing state
// changes after the fact.
// Rejected alternative: let TwitterService (the facade) call notificationService.onFollowed()
// itself after delegating to SocialGraph.follow(). Simpler wiring, but then SocialGraph isn't
// really a Subject — it's just a class the facade happens to call before also calling
// something else. That doesn't demonstrate Observer; it's two sequential method calls.
//
// Why two adjacency maps (following + followers), not one: getFollowers() and getFollowing()
// are both O(1) set lookups this way. A single "following" map would make getFollowers() an
// O(n) scan over every user's following set — the exact query q032's push-based Observer
// model couldn't answer at all without walking every User's private observer list.
public class SocialGraph {

    private final Map<String, Set<String>> following = new HashMap<>();
    private final Map<String, Set<String>> followers = new HashMap<>();
    private final List<TwitterEventObserver> observers = new ArrayList<>();

    public void addObserver(TwitterEventObserver observer) {
        observers.add(observer);
    }

    public Follow follow(String followerId, String followeeId) {
        if (followerId.equals(followeeId)) {
            throw new IllegalArgumentException("A user cannot follow themselves: " + followerId);
        }
        following.computeIfAbsent(followerId, k -> new HashSet<>()).add(followeeId);
        followers.computeIfAbsent(followeeId, k -> new HashSet<>()).add(followerId);

        Follow event = new Follow(followerId, followeeId, Instant.now());
        // Notify AFTER both maps are consistent — an observer reacting to onFollowed() (e.g.
        // NotificationService resolving usernames) must see a fully-updated graph, not a
        // half-written one.
        for (TwitterEventObserver observer : observers) {
            observer.onFollowed(event);
        }
        return event;
    }

    public void unfollow(String followerId, String followeeId) {
        following.getOrDefault(followerId, Set.of()).remove(followeeId);
        followers.getOrDefault(followeeId, Set.of()).remove(followerId);
        // What NOT to do: don't notify observers here or try to retract a past notification.
        // Notifications are an append-only event log ("alice followed you" stays true as
        // history even after alice unfollows) — same reasoning as Follow/Like being explicit,
        // immutable entities rather than mutable state.
    }

    public Set<String> getFollowing(String userId) {
        return Set.copyOf(following.getOrDefault(userId, Set.of()));
    }

    public Set<String> getFollowers(String userId) {
        return Set.copyOf(followers.getOrDefault(userId, Set.of()));
    }

    public boolean isFollowing(String followerId, String followeeId) {
        return following.getOrDefault(followerId, Set.of()).contains(followeeId);
    }

    public int getFollowerCount(String userId) {
        return followers.getOrDefault(userId, Set.of()).size();
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `mvn compile -pl ood`
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/SocialGraph.java
git commit -m "feat(q035): add SocialGraph as Observer Subject for follow events"
```

---

### Task 7: LikeService (Observer Subject)

**Files:**
- Create: `ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/LikeService.java`

- [ ] **Step 1: Write the file**

```java
package com.interview.ood.q035_twitter_service_layer.solution;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Pattern: Observer (Subject side) — same shape as SocialGraph, but for like events.
// Two Subjects rather than one shared "TwitterSubject" superclass: SocialGraph and LikeService
// have nothing else in common (different state, different guard conditions), and a shared
// base class would only exist to host the observer list + notify loop — four lines of
// duplication that isn't worth the coupling of forcing both through one supertype.
public class LikeService {

    private final Map<String, Like> likes = new HashMap<>();
    private final Map<String, Set<String>> likesByTweet = new HashMap<>();
    private final List<TwitterEventObserver> observers = new ArrayList<>();

    public void addObserver(TwitterEventObserver observer) {
        observers.add(observer);
    }

    public Like like(String userId, String tweetId) {
        String key = userId + ":" + tweetId;
        if (likes.containsKey(key)) {
            throw new IllegalStateException("User " + userId + " already liked tweet " + tweetId);
        }
        Like event = new Like(userId, tweetId, Instant.now());
        likes.put(key, event);
        likesByTweet.computeIfAbsent(tweetId, k -> new HashSet<>()).add(userId);

        for (TwitterEventObserver observer : observers) {
            observer.onLiked(event);
        }
        return event;
    }

    public void unlike(String userId, String tweetId) {
        String key = userId + ":" + tweetId;
        likes.remove(key);
        likesByTweet.getOrDefault(tweetId, Set.of()).remove(userId);
        // Same event-log reasoning as SocialGraph.unfollow(): no notification retraction here.
    }

    public int getLikeCount(String tweetId) {
        return likesByTweet.getOrDefault(tweetId, Set.of()).size();
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `mvn compile -pl ood`
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/LikeService.java
git commit -m "feat(q035): add LikeService as Observer Subject for like events"
```

---

### Task 8: NotificationService (concrete Observer)

**Files:**
- Create: `ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/NotificationService.java`

- [ ] **Step 1: Write the file**

```java
package com.interview.ood.q035_twitter_service_layer.solution;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// Pattern: Observer (concrete Observer side) — subscribes to both SocialGraph and LikeService
// as a TwitterEventObserver. This is the whole reason the interface exists: one class reacting
// to two different Subjects' state changes without either Subject knowing what happens next.
//
// Why NotificationService needs UserRepository + TweetRepository: the events it receives
// (Follow, Like) only carry IDs. Turning "u-123 followed u-456" into "alice followed you"
// requires a username lookup; turning a Like into "who gets notified" requires resolving the
// tweet's author. Keeping that resolution here (not in LikeService/SocialGraph) means the
// Subjects stay ignorant of notifications entirely — they just publish raw domain events.
//
// What NOT to do: don't have NotificationService push into FeedService's timeline directly
// (e.g. prepend the new tweet to a cached feed on every onFollowed/onLiked). That resurrects
// exactly the push-based Observer feed q032 already demonstrates doesn't scale for arbitrary
// historical queries (can't ask "show me tweets since 3 days ago" from a live-mutated cache).
// Notifications and the timeline are separate concerns solved separately: timeline stays pull
// (FeedService queries on demand), notifications stay push (this class reacts to events).
public class NotificationService implements TwitterEventObserver {

    private final UserRepository userRepository;
    private final TweetRepository tweetRepository;
    // Deque + addFirst, same shape as q032's Feed — O(1) prepend for "newest first" ordering,
    // instead of ArrayList.add(0, ...) which is O(n) per insert.
    private final Map<String, Deque<Notification>> byRecipient = new HashMap<>();

    public NotificationService(UserRepository userRepository, TweetRepository tweetRepository) {
        this.userRepository = userRepository;
        this.tweetRepository = tweetRepository;
    }

    @Override
    public void onFollowed(Follow follow) {
        String actorName = userRepository.findById(follow.followerId())
                .map(User::username)
                .orElse(follow.followerId());
        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                follow.followeeId(),
                follow.followerId(),
                NotificationType.FOLLOW,
                actorName + " followed you",
                Instant.now());
        store(notification);
    }

    @Override
    public void onLiked(Like like) {
        Optional<Tweet> tweet = tweetRepository.findById(like.tweetId());
        if (tweet.isEmpty()) {
            return;
        }
        String recipientId = tweet.get().authorId();
        // What NOT to do: notify yourself when you like your own tweet. Real product
        // behavior, and a genuine edge case this test suite covers — silently skipping it
        // here is the fix, not a defensive null-check elsewhere.
        if (recipientId.equals(like.userId())) {
            return;
        }
        String actorName = userRepository.findById(like.userId())
                .map(User::username)
                .orElse(like.userId());
        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                recipientId,
                like.userId(),
                NotificationType.LIKE,
                actorName + " liked your tweet",
                Instant.now());
        store(notification);
    }

    private void store(Notification notification) {
        byRecipient.computeIfAbsent(notification.recipientId(), k -> new ArrayDeque<>())
                .addFirst(notification);
    }

    public List<Notification> getNotifications(String userId) {
        return List.copyOf(byRecipient.getOrDefault(userId, new ArrayDeque<>()));
    }
}
```

- [ ] **Step 2: Verify it compiles**

Run: `mvn compile -pl ood`
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/NotificationService.java
git commit -m "feat(q035): add NotificationService as concrete Observer"
```

---

### Task 9: FeedService

**Files:**
- Create: `ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/FeedService.java`

- [ ] **Step 1: Write the file**

```java
package com.interview.ood.q035_twitter_service_layer.solution;

import java.util.Comparator;
import java.util.List;

// Algorithm: fan-out on read — pull every followee's tweets at query time, merge, sort, and
// take the top N. O(F * T) per call (F = followees, T = avg tweets per followee), no
// write-time cost. Contrast q032's fan-out-on-write Observer model: O(1) at read time but
// O(followers) at write time, and — the actual limitation this question exists to show —
// no way to ask "what did I miss since yesterday" without replaying every push.
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
```

- [ ] **Step 2: Verify it compiles**

Run: `mvn compile -pl ood`
Expected: `BUILD SUCCESS`

- [ ] **Step 3: Commit**

```bash
git add ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/FeedService.java
git commit -m "feat(q035): add FeedService (pull-model timeline)"
```

---

### Task 10: TwitterService facade + TwitterMain demo

**Files:**
- Create: `ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/TwitterService.java`
- Create: `ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/TwitterMain.java`

- [ ] **Step 1: Write `TwitterService.java`**

```java
package com.interview.ood.q035_twitter_service_layer.solution;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// Pattern: Facade — the single entry point coordinating five independently-testable services.
// Its only Observer-related job is wiring the subscription ONCE at construction
// (socialGraph.addObserver / likeService.addObserver); it never calls
// notificationService.onFollowed()/onLiked() itself. That distinction matters: if the facade
// published events, adding a second Subject later would mean editing TwitterService every
// time, instead of the new Subject just registering the same observer.
public class TwitterService {

    private static final int DEFAULT_TIMELINE_LIMIT = 50;
    private static final int MAX_CONTENT_LENGTH = 280;

    private final UserRepository userRepository;
    private final TweetRepository tweetRepository;
    private final SocialGraph socialGraph;
    private final FeedService feedService;
    private final LikeService likeService;
    private final NotificationService notificationService;

    public TwitterService() {
        this.userRepository = new UserRepository();
        this.tweetRepository = new TweetRepository();
        this.socialGraph = new SocialGraph();
        this.likeService = new LikeService();
        this.feedService = new FeedService(socialGraph, tweetRepository);
        this.notificationService = new NotificationService(userRepository, tweetRepository);

        // Subscribe once, here — this is the only place SocialGraph/LikeService and
        // NotificationService know about each other.
        socialGraph.addObserver(notificationService);
        likeService.addObserver(notificationService);
    }

    public User register(String username) {
        User user = new User(UUID.randomUUID().toString(), username, "");
        return userRepository.save(user);
    }

    public Tweet post(String userId, String content) {
        if (content == null || content.isBlank() || content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException(
                    "Tweet content must be 1-" + MAX_CONTENT_LENGTH + " characters");
        }
        Tweet tweet = new Tweet(UUID.randomUUID().toString(), userId, content, null, Instant.now());
        return tweetRepository.save(tweet);
    }

    public Follow follow(String followerId, String followeeId) {
        return socialGraph.follow(followerId, followeeId);
    }

    public Like like(String userId, String tweetId) {
        return likeService.like(userId, tweetId);
    }

    public List<Tweet> getTimeline(String userId) {
        return feedService.getTimeline(userId, DEFAULT_TIMELINE_LIMIT);
    }

    // Accessors for tests — expose internal services directly rather than duplicating every
    // query method on the facade (isFollowing, getLikeCount, getNotifications, ...). Acceptable
    // in an interview-scale system; a production facade might narrow this surface.
    public SocialGraph getSocialGraph() {
        return socialGraph;
    }

    public LikeService getLikeService() {
        return likeService;
    }

    public NotificationService getNotificationService() {
        return notificationService;
    }
}
```

- [ ] **Step 2: Write `TwitterMain.java`**

```java
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
```

- [ ] **Step 3: Verify it compiles and the demo runs**

Run: `mvn compile -pl ood`
Expected: `BUILD SUCCESS`

`exec-maven-plugin` isn't configured in this project's `pom.xml`, so run the compiled class directly with `java -cp`:

Run: `java -cp ood/target/classes com.interview.ood.q035_twitter_service_layer.solution.TwitterMain`
Expected: prints the 6 `===` sections above; alice's notifications after the like show one `LIKE` entry, and stay unchanged after alice likes her own tweet.

- [ ] **Step 4: Commit**

```bash
git add ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/TwitterService.java \
        ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/TwitterMain.java
git commit -m "feat(q035): add TwitterService facade wiring Observer subscription, and TwitterMain demo"
```

---

### Task 11: SolutionTest.java — write and verify all 8 tests

**Files:**
- Create: `ood/src/test/java/com/interview/ood/q035_twitter_service_layer/solution/SolutionTest.java`

- [ ] **Step 1: Write the test file**

```java
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
```

- [ ] **Step 2: Run the tests**

Run: `mvn test -pl ood -Dtest=com.interview.ood.q035_twitter_service_layer.solution.SolutionTest`
Expected: `Tests run: 8, Failures: 0, Errors: 0` and `BUILD SUCCESS`.

If any test fails, the bug is in the Task 3–10 implementation (every method's exact behavior was already specified in those tasks' code) — fix the implementation file, not the test, then re-run this command until all 8 pass.

- [ ] **Step 3: Commit**

```bash
git add ood/src/test/java/com/interview/ood/q035_twitter_service_layer/solution/SolutionTest.java
git commit -m "test(q035): add SolutionTest covering timeline, social graph, likes, and Observer notifications"
```

---

### Task 12: answer/ placeholders + folder mirroring

**Files:**
- Create: `ood/src/main/java/com/interview/ood/q035_twitter_service_layer/answer/.gitkeep`
- Create: `ood/src/test/java/com/interview/ood/q035_twitter_service_layer/answer/.gitkeep`

- [ ] **Step 1: Create both empty placeholder files**

```bash
touch ood/src/main/java/com/interview/ood/q035_twitter_service_layer/answer/.gitkeep
touch ood/src/test/java/com/interview/ood/q035_twitter_service_layer/answer/.gitkeep
```

- [ ] **Step 2: Verify final folder layout matches the spec**

Run: `find ood/src/main/java/com/interview/ood/q035_twitter_service_layer ood/src/test/java/com/interview/ood/q035_twitter_service_layer -type f | sort`

Expected output (paths, order may vary slightly by `find` implementation):
```
ood/src/main/java/com/interview/ood/q035_twitter_service_layer/answer/.gitkeep
ood/src/main/java/com/interview/ood/q035_twitter_service_layer/question.md
ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/FeedService.java
ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/Follow.java
ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/Like.java
ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/LikeService.java
ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/Notification.java
ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/NotificationService.java
ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/NotificationType.java
ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/SocialGraph.java
ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/Tweet.java
ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/TweetRepository.java
ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/TwitterEventObserver.java
ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/TwitterMain.java
ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/TwitterService.java
ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/User.java
ood/src/main/java/com/interview/ood/q035_twitter_service_layer/solution/UserRepository.java
ood/src/test/java/com/interview/ood/q035_twitter_service_layer/answer/.gitkeep
ood/src/test/java/com/interview/ood/q035_twitter_service_layer/solution/SolutionTest.java
```

- [ ] **Step 3: Commit**

```bash
git add ood/src/main/java/com/interview/ood/q035_twitter_service_layer/answer/.gitkeep \
        ood/src/test/java/com/interview/ood/q035_twitter_service_layer/answer/.gitkeep
git commit -m "chore(q035): add empty answer/ placeholders, completing solution/answer migration"
```

---

### Task 13: Full module regression check

**Files:** none (verification only)

- [ ] **Step 1: Run the full `ood` module test suite**

Run: `mvn test -pl ood`
Expected: `BUILD SUCCESS`, all tests pass including the new `SolutionTest` (8 tests) and every pre-existing `ood` test (q013 through q044) unaffected by this change.

- [ ] **Step 2: Confirm no stray references to the deleted flat files remain**

Run: `grep -rl "q035_twitter_service_layer.MyAnswer" ood/src || echo "none found"`
Expected: `none found`

This is the final task — no commit needed if Step 1 and Step 2 both pass cleanly (nothing new to stage).
