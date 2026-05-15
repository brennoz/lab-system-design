package com.lab.feed.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserRepository extends JpaRepository<UserJpaEntity, UUID> {

    Optional<UserJpaEntity> findByEmail(String email);

    List<UserJpaEntity> findAllByEmailIn(List<String> emails);

    @Modifying @Transactional
    @Query("UPDATE UserJpaEntity u SET u.followerCount = u.followerCount + 1 WHERE u.email = :email")
    void incrementFollowerCount(@Param("email") String email);

    @Modifying @Transactional
    @Query("UPDATE UserJpaEntity u SET u.followerCount = u.followerCount - 1 WHERE u.email = :email")
    void decrementFollowerCount(@Param("email") String email);
}
