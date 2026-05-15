package com.lab.feed.infrastructure.persistence;

import com.lab.feed.domain.model.Post;
import com.lab.feed.domain.port.PostRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

// Adapter: maps between Post domain record and PostJpaEntity
public class JpaPostRepository implements PostRepository {

    private final SpringDataPostRepository delegate;

    public JpaPostRepository(SpringDataPostRepository delegate) {
        this.delegate = delegate;
    }

    @Override
    public Post save(Post post) {
        PostJpaEntity entity = new PostJpaEntity(post.id(), post.authorId(), post.content(),
                post.likeCount(), post.createdAt());
        PostJpaEntity saved = delegate.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Post> findById(UUID id) {
        return delegate.findById(id).map(this::toDomain);
    }

    @Override
    public List<Post> findAllByIds(List<UUID> ids) {
        return delegate.findAllById(ids).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Post> findRecentByAuthorId(String authorId, int limit) {
        return delegate.findRecentByAuthorId(authorId, limit).stream().map(this::toDomain).toList();
    }

    private Post toDomain(PostJpaEntity e) {
        return new Post(e.getId(), e.getAuthorId(), e.getContent(), e.getLikeCount(), e.getCreatedAt());
    }
}
