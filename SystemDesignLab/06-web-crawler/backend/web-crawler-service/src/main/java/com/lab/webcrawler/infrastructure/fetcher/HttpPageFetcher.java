package com.lab.webcrawler.infrastructure.fetcher;

import com.lab.webcrawler.domain.port.PageFetcherPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

// Pattern: HTTP adapter — wraps RestTemplate; domain stays ignorant of HTTP mechanics
public class HttpPageFetcher implements PageFetcherPort {

    private static final String USER_AGENT = "LabCrawler/1.0 (educational; +https://github.com/brennoz77)";

    private final RestTemplate restTemplate;

    public HttpPageFetcher(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String fetch(String url) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        String body = response.getBody();
        return body != null ? body : "";
    }
}
