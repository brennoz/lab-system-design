package com.lab.urlshortener.api;

import com.lab.urlshortener.api.dto.ShortenRequest;
import com.lab.urlshortener.api.dto.ShortenResponse;
import com.lab.urlshortener.application.usecase.ResolveUrlUseCase;
import com.lab.urlshortener.application.usecase.ShortenUrlUseCase;
import com.lab.urlshortener.domain.model.ShortCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@Tag(name = "URL Shortener", description = "Shorten URLs and resolve short codes to originals")
public class UrlShortenerController {

    private final ShortenUrlUseCase shortenUrlUseCase;
    private final ResolveUrlUseCase resolveUrlUseCase;

    public UrlShortenerController(ShortenUrlUseCase shortenUrlUseCase,
                                  ResolveUrlUseCase resolveUrlUseCase) {
        this.shortenUrlUseCase = shortenUrlUseCase;
        this.resolveUrlUseCase = resolveUrlUseCase;
    }

    @PostMapping("/api/v1/shorten")
    @Operation(summary = "Shorten a URL", description = "Persists the URL and returns a Base62 short code")
    public ResponseEntity<ShortenResponse> shorten(@Valid @RequestBody ShortenRequest request,
                                                   HttpServletRequest httpRequest) {
        ShortCode code = shortenUrlUseCase.shorten(request.originalUrl());
        String baseUrl = httpRequest.getRequestURL().toString()
                .replace("/api/v1/shorten", "");
        String shortUrl = baseUrl + "/r/" + code.value();
        return ResponseEntity.ok(new ShortenResponse(code.value(), shortUrl));
    }

    // Why 302 not 301: 301 is cached permanently by browsers — dangerous if we ever delete or update links
    // /r/{code} at root — short URLs must be short.ly/1, not short.ly/api/v1/r/1
    @GetMapping("/r/{code}")
    @Operation(summary = "Redirect to original URL")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        String originalUrl = resolveUrlUseCase.resolve(code);
        return ResponseEntity.status(302)
                .location(URI.create(originalUrl))
                .build();
    }
}
