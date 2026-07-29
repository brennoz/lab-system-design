# twitter — Lesson Index

| File | Concepts covered |
|---|---|
| [0001-tweet-model-observer.html](0001-tweet-model-observer.html) | Polymorphic Tweet hierarchy (Original/Retweet/Reply), Template Method (displayContent), Builder (TweetBuilder, 280-char validation), Observer Pattern (FeedObserver, fan-out on write), CopyOnWriteArrayList for observer list, LinkedList/Deque for timeline, fan-out on write vs read trade-off |
| [0002-twitter-service-layer.html](0002-twitter-service-layer.html) | Service Layer vs Rich Domain Model comparison, explicit Follow + Like records, SocialGraph service (adjacency list, O(1) forward+reverse lookup), TweetRepository (Map-based, secondary index by author), FeedService (fan-out on read via stream merge), LikeService, SRP/DIP/OCP comparison between both models, when to use each |
| [0003-observer-notifications-full-walkthrough.html](0003-observer-notifications-full-walkthrough.html) | Full solution walkthrough for q035's current form: Observer Pattern applied to a second, independent concern (notifications, push) alongside the pull-model timeline from Lesson 02; SocialGraph/LikeService as Observer Subjects, NotificationService as concrete Observer, TwitterEventObserver interface, TDD red→green narrative for two real bugs (getOrDefault(key, Set.of()).remove() throwing on absent keys; mutable-collection leak), self-notification guard, append-only notification log, AI Engineering Extension (notification ranking/batching) |

## Design note (applies from q033 onward)

OOD exercises use a **production-leaning structure**:
- Domain objects are data containers (`User`, `Tweet`, `Book`) — no embedded graph logic
- Explicit relationship entities (`Follow`, `Loan`, `Like`) that map to DB rows
- In-memory repositories (`Map<String, T>`) simulating persistence — still no Spring/JPA
- Separate service/manager classes own the logic and queries (`SocialGraph`, `FeedService`, `LibraryService`)
- Always queryable: "who follows whom?", "what are Alice's active loans?"
