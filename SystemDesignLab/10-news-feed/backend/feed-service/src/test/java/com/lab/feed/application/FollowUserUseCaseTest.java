package com.lab.feed.application;

import com.lab.feed.application.usecase.FollowUserUseCase;
import com.lab.feed.domain.model.Follow;
import com.lab.feed.domain.port.FollowRepository;
import com.lab.feed.domain.port.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FollowUserUseCaseTest {

    @Mock FollowRepository followRepository;
    @Mock UserRepository userRepository;
    @InjectMocks FollowUserUseCase useCase;

    @Test
    void saves_follow_and_increments_followee_count() {
        when(followRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Follow follow = useCase.follow("alice@test.com", "bob@test.com");

        assertThat(follow.followerId()).isEqualTo("alice@test.com");
        assertThat(follow.followeeId()).isEqualTo("bob@test.com");
        verify(userRepository).incrementFollowerCount("bob@test.com");
    }

    @Test
    void unfollow_deletes_and_decrements_count() {
        UUID followId = UUID.randomUUID();
        Follow follow = new Follow(followId, "alice@test.com", "bob@test.com", Instant.now());
        when(followRepository.findByFollowerAndFollowee("alice@test.com", "bob@test.com"))
                .thenReturn(Optional.of(follow));

        useCase.unfollow("alice@test.com", "bob@test.com");

        verify(followRepository).delete(followId);
        verify(userRepository).decrementFollowerCount("bob@test.com");
    }
}
