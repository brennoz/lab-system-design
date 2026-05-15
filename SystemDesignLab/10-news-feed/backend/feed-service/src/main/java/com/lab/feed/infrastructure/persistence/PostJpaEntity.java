package com.lab.feed.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "posts")
public class PostJpaEntity {

    @Id
    UUID id;
    String authorId;
    @Column(length = 2000)
    String content;
    int likeCount;
    Instant createdAt;

    protected PostJpaEntity() {}

    public PostJpaEntity(UUID id, String authorId, String content, int likeCount, Instant createdAt) {
        this.id = id;
        this.authorId = authorId;
        this.content = content;
        this.likeCount = likeCount;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public String getAuthorId() { return authorId; }
    public String getContent() { return content; }
    public int getLikeCount() { return likeCount; }
    public Instant getCreatedAt() { return createdAt; }
}
