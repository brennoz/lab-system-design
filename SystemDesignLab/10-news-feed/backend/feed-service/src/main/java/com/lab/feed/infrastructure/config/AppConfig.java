package com.lab.feed.infrastructure.config;

import com.lab.feed.application.service.FanOutService;
import com.lab.feed.application.service.RankingService;
import com.lab.feed.application.usecase.*;
import com.lab.feed.domain.port.*;
import com.lab.feed.infrastructure.feed.RedisFeedAdapter;
import com.lab.feed.infrastructure.persistence.*;
import com.lab.feed.infrastructure.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class AppConfig {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Bean
    public JwtService jwtService() {
        return new JwtService(jwtSecret, jwtExpirationMs);
    }

    @Bean
    public PostRepository postRepository(SpringDataPostRepository delegate) {
        return new JpaPostRepository(delegate);
    }

    @Bean
    public FollowRepository followRepository(SpringDataFollowRepository delegate) {
        return new JpaFollowRepository(delegate);
    }

    @Bean
    public UserRepository userRepository(SpringDataUserRepository delegate) {
        return new JpaUserRepository(delegate);
    }

    @Bean
    public FeedPort feedPort(StringRedisTemplate redis) {
        return new RedisFeedAdapter(redis);
    }

    @Bean
    public RankingService rankingService() {
        return new RankingService();
    }

    @Bean
    public FanOutService fanOutService(FollowRepository followRepository, FeedPort feedPort,
                                       RankingService rankingService) {
        return new FanOutService(followRepository, feedPort, rankingService);
    }

    @Bean
    public CreatePostUseCase createPostUseCase(PostRepository postRepository,
                                               UserRepository userRepository,
                                               FanOutService fanOutService) {
        return new CreatePostUseCase(postRepository, userRepository, fanOutService);
    }

    @Bean
    public GetFeedUseCase getFeedUseCase(FeedPort feedPort, PostRepository postRepository,
                                         FollowRepository followRepository,
                                         UserRepository userRepository,
                                         RankingService rankingService) {
        return new GetFeedUseCase(feedPort, postRepository, followRepository, userRepository, rankingService);
    }

    @Bean
    public LikePostUseCase likePostUseCase(PostRepository postRepository,
                                            FanOutService fanOutService,
                                            RankingService rankingService) {
        return new LikePostUseCase(postRepository, fanOutService, rankingService);
    }

    @Bean
    public FollowUserUseCase followUserUseCase(FollowRepository followRepository,
                                               UserRepository userRepository) {
        return new FollowUserUseCase(followRepository, userRepository);
    }
}
