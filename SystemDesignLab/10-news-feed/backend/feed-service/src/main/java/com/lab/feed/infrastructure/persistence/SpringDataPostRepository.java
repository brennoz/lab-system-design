package com.lab.feed.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface SpringDataPostRepository extends JpaRepository<PostJpaEntity, UUID> {

    @Query("SELECT p FROM PostJpaEntity p WHERE p.authorId = :authorId ORDER BY p.createdAt DESC LIMIT :limit")
    List<PostJpaEntity> findRecentByAuthorId(@Param("authorId") String authorId, @Param("limit") int limit);
}
