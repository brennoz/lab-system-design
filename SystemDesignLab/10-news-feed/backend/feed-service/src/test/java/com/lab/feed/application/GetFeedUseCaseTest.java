package com.lab.feed.application;

import com.lab.feed.application.service.RankingService;
import com.lab.feed.application.usecase.GetFeedUseCase;
import com.lab.feed.domain.model.FeedItem;
import com.lab.feed.domain.model.Post;
import com.lab.feed.domain.model.User;
import com.lab.feed.domain.port.FeedPort;
import com.lab.feed.domain.port.FollowRepository;
import com.lab.feed.domain.port.PostRepository;
import com.lab.feed.domain.port.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetFeedUseCaseTest {

    @Mock FeedPort feedPort;
    @Mock PostRepository postRepository;
    @Mock FollowRepository followRepository;
    @Mock UserRepository userRepository;
    @Mock RankingService rankingService;
    @InjectMocks GetFeedUseCase useCase;

    @Test
    void merges_redis_posts_with_celebrity_posts() {
        UUID postId = UUID.randomUUID();
        Post regularPost = new Post(postId, "bob@test.com", "Hi", 0, Instant.now());
        Post celebPost = new Post(UUID.randomUUID(), "star@test.com", "Celeb!", 100, Instant.now());

        User celeb = new User(UUID.randomUUID(), "star@test.com", "h", 50_000, Instant.now());

        when(feedPort.getTopPostIds("alice@test.com", 60)).thenReturn(List.of(postId));
        when(postRepository.findAllByIds(List.of(postId))).thenReturn(List.of(regularPost));
        when(followRepository.findFolloweeIds("alice@test.com")).thenReturn(List.of("star@test.com"));
        when(userRepository.findAllByEmails(List.of("star@test.com"))).thenReturn(List.of(celeb));
        when(postRepository.findRecentByAuthorId("star@test.com", 20)).thenReturn(List.of(celebPost));
        when(rankingService.score(any())).thenReturn(1_000_000.0);

        List<FeedItem> feed = useCase.getFeed("alice@test.com", 0, 20);

        assertThat(feed).hasSize(2);
    }

    @Test
    void returns_only_redis_posts_when_no_celebrities_followed() {
        UUID postId = UUID.randomUUID();
        Post post = new Post(postId, "bob@test.com", "Hi", 0, Instant.now());
        User regularUser = new User(UUID.randomUUID(), "bob@test.com", "h", 50, Instant.now());

        when(feedPort.getTopPostIds("alice@test.com", 60)).thenReturn(List.of(postId));
        when(postRepository.findAllByIds(List.of(postId))).thenReturn(List.of(post));
        when(followRepository.findFolloweeIds("alice@test.com")).thenReturn(List.of("bob@test.com"));
        when(userRepository.findAllByEmails(List.of("bob@test.com"))).thenReturn(List.of(regularUser));
        when(rankingService.score(any())).thenReturn(1_000_000.0);

        List<FeedItem> feed = useCase.getFeed("alice@test.com", 0, 20);

        assertThat(feed).hasSize(1);
        verify(postRepository, never()).findRecentByAuthorId(any(), anyInt());
    }
}
