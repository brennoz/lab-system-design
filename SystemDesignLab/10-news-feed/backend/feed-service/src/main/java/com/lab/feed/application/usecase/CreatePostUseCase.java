package com.lab.feed.application.usecase;

import com.lab.feed.application.service.FanOutService;
import com.lab.feed.domain.model.Post;
import com.lab.feed.domain.model.User;
import com.lab.feed.domain.port.PostRepository;
import com.lab.feed.domain.port.UserRepository;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

public class CreatePostUseCase {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final FanOutService fanOutService;

    public CreatePostUseCase(PostRepository postRepository, UserRepository userRepository,
                              FanOutService fanOutService) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.fanOutService = fanOutService;
    }

    public Post create(String authorEmail, String content) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Post post = postRepository.save(Post.of(authorEmail, content));
        fanOutService.fanOut(post, author);
        return post;
    }
}
