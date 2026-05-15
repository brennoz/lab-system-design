package com.lab.feed.application.usecase;

import com.lab.feed.domain.model.Follow;
import com.lab.feed.domain.port.FollowRepository;
import com.lab.feed.domain.port.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

public class FollowUserUseCase {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    public FollowUserUseCase(FollowRepository followRepository, UserRepository userRepository) {
        this.followRepository = followRepository;
        this.userRepository = userRepository;
    }

    public Follow follow(String followerEmail, String followeeEmail) {
        Follow follow = followRepository.save(Follow.of(followerEmail, followeeEmail));
        // Denormalized count updated here — avoids N queries per feed read for celebrity check
        userRepository.incrementFollowerCount(followeeEmail);
        return follow;
    }

    public void unfollow(String followerEmail, String followeeEmail) {
        Follow follow = followRepository.findByFollowerAndFollowee(followerEmail, followeeEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Follow not found"));
        followRepository.delete(follow.id());
        userRepository.decrementFollowerCount(followeeEmail);
    }
}
