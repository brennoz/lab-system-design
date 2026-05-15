package com.lab.feed.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataFollowRepository extends JpaRepository<FollowJpaEntity, UUID> {

    @Query("SELECT f.followerId FROM FollowJpaEntity f WHERE f.followeeId = :followeeId")
    List<String> findFollowerIds(@Param("followeeId") String followeeId);

    @Query("SELECT f.followeeId FROM FollowJpaEntity f WHERE f.followerId = :followerId")
    List<String> findFolloweeIds(@Param("followerId") String followerId);

    Optional<FollowJpaEntity> findByFollowerIdAndFolloweeId(String followerId, String followeeId);
}
