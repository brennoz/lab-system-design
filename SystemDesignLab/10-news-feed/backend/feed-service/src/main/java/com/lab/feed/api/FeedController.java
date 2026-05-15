package com.lab.feed.api;

import com.lab.feed.api.dto.FeedItemResponse;
import com.lab.feed.application.usecase.GetFeedUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/feed")
public class FeedController {

    private final GetFeedUseCase getFeedUseCase;

    public FeedController(GetFeedUseCase getFeedUseCase) {
        this.getFeedUseCase = getFeedUseCase;
    }

    @GetMapping
    public ResponseEntity<List<FeedItemResponse>> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(
                getFeedUseCase.getFeed(user.getUsername(), page, size)
                        .stream().map(FeedItemResponse::from).toList());
    }
}
