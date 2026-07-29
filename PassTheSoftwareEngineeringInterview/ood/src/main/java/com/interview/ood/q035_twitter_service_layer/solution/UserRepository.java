package com.interview.ood.q035_twitter_service_layer.solution;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Design decision: in-memory Map standing in for a real users table — this project's stated
// convention (see q033 onward) is Map-based repos with no ORM, since these questions test OOD
// reasoning, not persistence frameworks.
// Rejected alternative: a List<User> scanned linearly on findById() — rejected because it
// turns an O(1) lookup into O(n), and this project's repos exist specifically to demonstrate
// the Map-based repository shape used consistently elsewhere here (TweetRepository, and
// SocialGraph's adjacency maps).
// What NOT to do: don't expect callers to mutate a User returned by findById() in place —
// User is an immutable record, so "updating" a user means constructing a new one and calling
// save() again, not mutating fields (which records don't even allow).
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
