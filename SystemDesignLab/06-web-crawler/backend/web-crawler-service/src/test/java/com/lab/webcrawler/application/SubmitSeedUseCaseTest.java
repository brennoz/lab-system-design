package com.lab.webcrawler.application;

import com.lab.webcrawler.application.usecase.SubmitSeedUseCase;
import com.lab.webcrawler.domain.model.CrawlUrl;
import com.lab.webcrawler.domain.port.SeenFilterPort;
import com.lab.webcrawler.domain.port.UrlQueuePort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class SubmitSeedUseCaseTest {

    private final SeenFilterPort seenFilter = mock(SeenFilterPort.class);
    private final UrlQueuePort urlQueue = mock(UrlQueuePort.class);
    private final SubmitSeedUseCase useCase = new SubmitSeedUseCase(seenFilter, urlQueue);

    @Test
    void enqueues_unseen_seed_and_marks_seen() {
        when(seenFilter.mightContain("https://bbc.com")).thenReturn(false);

        useCase.submit(List.of("https://bbc.com"));

        verify(seenFilter).add("https://bbc.com");
        verify(urlQueue).enqueue(new CrawlUrl("https://bbc.com", 0));
    }

    @Test
    void skips_already_seen_seed() {
        when(seenFilter.mightContain("https://bbc.com")).thenReturn(true);

        useCase.submit(List.of("https://bbc.com"));

        verify(seenFilter, never()).add(any());
        verify(urlQueue, never()).enqueue(any());
    }
}
