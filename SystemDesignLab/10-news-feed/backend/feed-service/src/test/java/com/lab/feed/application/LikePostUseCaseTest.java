package com.lab.feed.application;

import com.lab.feed.application.service.FanOutService;
import com.lab.feed.application.service.RankingService;
import com.lab.feed.application.usecase.LikePostUseCase;
import com.lab.feed.domain.model.Post;
import com.lab.feed.domain.model.PostNotFoundException;
import com.lab.feed.domain.port.PostRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikePostUseCaseTest {

    @Mock PostRepository postRepository;
    @Mock FanOutService fanOutService;
    @Mock RankingService rankingService;
    @InjectMocks LikePostUseCase useCase;

    @Test
    void increments_like_count_and_rescores_feed() {
        UUID postId = UUID.randomUUID();
        Post post = new Post(postId, "bob@test.com", "Hi", 0, Instant.now());
        Post liked = post.withLikeCount(1);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any())).thenReturn(liked);
        when(rankingService.score(liked)).thenReturn(1_749_000.0);

        Post result = useCase.like(postId);

        assertThat(result.likeCount()).isEqualTo(1);
        // re-score fan-out triggered for regular author
        verify(fanOutService).rescorePost(liked, 1_749_000.0);
    }

    @Test
    void throws_when_post_not_found() {
        UUID postId = UUID.randomUUID();
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.like(postId))
                .isInstanceOf(PostNotFoundException.class);
    }
}
