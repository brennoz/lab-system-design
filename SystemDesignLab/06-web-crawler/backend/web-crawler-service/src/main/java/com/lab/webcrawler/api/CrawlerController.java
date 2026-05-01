package com.lab.webcrawler.api;

import com.lab.webcrawler.api.dto.SeedRequest;
import com.lab.webcrawler.application.usecase.SubmitSeedUseCase;
import com.lab.webcrawler.domain.port.PageRepository;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
public class CrawlerController {

    private final SubmitSeedUseCase submitSeedUseCase;
    private final PageRepository pageRepository;

    public CrawlerController(SubmitSeedUseCase submitSeedUseCase, PageRepository pageRepository) {
        this.submitSeedUseCase = submitSeedUseCase;
        this.pageRepository = pageRepository;
    }

    @Operation(summary = "Submit seed URLs to start crawling")
    @PostMapping("/api/v1/seeds")
    public ResponseEntity<Void> submitSeeds(@Valid @RequestBody SeedRequest request) {
        submitSeedUseCase.submit(request.urls());
        return ResponseEntity.accepted().build();
    }

    @Operation(summary = "Get crawl status")
    @GetMapping("/api/v1/status")
    public ResponseEntity<Map<String, Long>> status() {
        return ResponseEntity.ok(Map.of("crawledPages", pageRepository.countCrawled()));
    }
}
