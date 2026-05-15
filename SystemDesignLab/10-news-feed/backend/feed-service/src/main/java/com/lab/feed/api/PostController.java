package com.lab.feed.api;

import com.lab.feed.api.dto.CreatePostRequest;
import com.lab.feed.api.dto.PostResponse;
import com.lab.feed.application.usecase.CreatePostUseCase;
import com.lab.feed.application.usecase.LikePostUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/posts")
public class PostController {

    private final CreatePostUseCase createPostUseCase;
    private final LikePostUseCase likePostUseCase;

    public PostController(CreatePostUseCase createPostUseCase, LikePostUseCase likePostUseCase) {
        this.createPostUseCase = createPostUseCase;
        this.likePostUseCase = likePostUseCase;
    }

    @PostMapping
    public ResponseEntity<PostResponse> create(@RequestBody CreatePostRequest req,
                                               @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PostResponse.from(createPostUseCase.create(user.getUsername(), req.content())));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<PostResponse> like(@PathVariable UUID id) {
        return ResponseEntity.ok(PostResponse.from(likePostUseCase.like(id)));
    }
}
