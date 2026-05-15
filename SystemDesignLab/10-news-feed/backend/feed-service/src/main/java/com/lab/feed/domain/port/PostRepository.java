package com.lab.feed.domain.port;

import com.lab.feed.domain.model.Post;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Port: secondary (driven) — persistence boundary for posts
public interface PostRepository {
    Post save(Post post);
    Optional<Post> findById(UUID id);
    List<Post> findAllByIds(List<UUID> ids);
    List<Post> findRecentByAuthorId(String authorId, int limit);
}
