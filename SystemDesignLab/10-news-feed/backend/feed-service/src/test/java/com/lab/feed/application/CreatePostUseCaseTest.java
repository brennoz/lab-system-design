package com.lab.feed.application;

import com.lab.feed.application.service.FanOutService;
import com.lab.feed.application.usecase.CreatePostUseCase;
import com.lab.feed.domain.model.Post;
import com.lab.feed.domain.model.User;
import com.lab.feed.domain.port.PostRepository;
import com.lab.feed.domain.port.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class CreatePostUseCaseTest {

    @Mock PostRepository postRepository;
    @Mock UserRepository userRepository;
    @Mock FanOutService fanOutService;
    @InjectMocks CreatePostUseCase useCase;

    @Test
    void saves_post_and_triggers_fanout() {
        User author = new User(UUID.randomUUID(), "alice@test.com", "hash", 100, Instant.now());
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(author));
        when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Post result = useCase.create("alice@test.com", "Hello feed!");

        assertThat(result.authorId()).isEqualTo("alice@test.com");
        assertThat(result.content()).isEqualTo("Hello feed!");
        assertThat(result.likeCount()).isZero();
        verify(fanOutService).fanOut(result, author);
    }

    @Test
    void does_not_fanout_for_celebrity() {
        // FanOutService decides internally — UseCase always calls it regardless
        User celebrity = new User(UUID.randomUUID(), "star@test.com", "hash", 50_000, Instant.now());
        when(userRepository.findByEmail("star@test.com")).thenReturn(Optional.of(celebrity));
        when(postRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.create("star@test.com", "Big news!");

        verify(fanOutService).fanOut(any(Post.class), eq(celebrity));
    }
}
