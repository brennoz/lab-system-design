package com.lab.feed.api;

import com.lab.feed.api.dto.FollowRequest;
import com.lab.feed.application.usecase.FollowUserUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/follows")
public class FollowController {

    private final FollowUserUseCase followUserUseCase;

    public FollowController(FollowUserUseCase followUserUseCase) {
        this.followUserUseCase = followUserUseCase;
    }

    @PostMapping
    public ResponseEntity<Void> follow(@RequestBody FollowRequest req,
                                       @AuthenticationPrincipal UserDetails user) {
        followUserUseCase.follow(user.getUsername(), req.followeeEmail());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping
    public ResponseEntity<Void> unfollow(@RequestBody FollowRequest req,
                                          @AuthenticationPrincipal UserDetails user) {
        followUserUseCase.unfollow(user.getUsername(), req.followeeEmail());
        return ResponseEntity.noContent().build();
    }
}
