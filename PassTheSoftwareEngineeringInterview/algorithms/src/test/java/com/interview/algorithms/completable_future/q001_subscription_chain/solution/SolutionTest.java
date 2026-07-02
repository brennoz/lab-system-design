package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolutionTest {

    // Mockito: @Mock on INTERFACE — JDK proxy, clean and preferred over mocking concrete classes.
    // Spring Boot equivalent: @MockBean registers the mock in the Spring application context.
    // Here: @Mock is enough — no Spring context needed for unit tests.
    @Mock SubscriptionService subscriptionService;
    @Mock PricingService pricingService;
    @Mock AccountService accountService;

    Solution underTest;

    @BeforeEach
    void setUp() {
        // q001 has no timeout — constructor takes 3 services only
        underTest = new Solution(subscriptionService, pricingService, accountService);
    }

    @Test
    void enrichReturnsCombinedSubscriptionAndAccount() throws Exception {
        var sub        = new Subscription("sub-1", "acc-1", "ANNUAL", BigDecimal.valueOf(1200));
        var discounted = new DiscountedSubscription("sub-1", "acc-1", "ANNUAL", BigDecimal.valueOf(1080));
        var account    = new Account("acc-1", "Acme Corp", "billing@acme.com");

        // CF: completedFuture — already-resolved CF. Callbacks fire synchronously on the calling thread.
        //     No thread pool involved in tests — deterministic and fast.
        when(subscriptionService.findById("sub-1")).thenReturn(CompletableFuture.completedFuture(sub));
        when(pricingService.applyDiscount(sub)).thenReturn(discounted);
        when(accountService.findById("acc-1")).thenReturn(CompletableFuture.completedFuture(account));

        EnrichedSubscription result = underTest.enrich("sub-1").get();

        assertEquals(discounted, result.subscription());
        assertEquals(account, result.account());
    }

    @Test
    void accountFetchedUsingAccountIdFromDiscountedSubscription() throws Exception {
        var sub        = new Subscription("sub-1", "acc-99", "MONTHLY", BigDecimal.valueOf(100));
        var discounted = new DiscountedSubscription("sub-1", "acc-99", "MONTHLY", BigDecimal.valueOf(90));
        var account    = new Account("acc-99", "Beta Ltd", "pay@beta.com");

        when(subscriptionService.findById("sub-1")).thenReturn(CompletableFuture.completedFuture(sub));
        when(pricingService.applyDiscount(sub)).thenReturn(discounted);
        when(accountService.findById("acc-99")).thenReturn(CompletableFuture.completedFuture(account));

        underTest.enrich("sub-1").get();

        // Mockito: verify() asserts the mock was called with specific args.
        // Catches bugs where implementation hardcodes an accountId or reads from the wrong object.
        verify(accountService).findById("acc-99");
    }

    @Test
    void discountAmountReflectedInResult() throws Exception {
        var sub        = new Subscription("sub-1", "acc-1", "ANNUAL", BigDecimal.valueOf(1200));
        var discounted = new DiscountedSubscription("sub-1", "acc-1", "ANNUAL", BigDecimal.valueOf(1080));
        var account    = new Account("acc-1", "Acme Corp", "billing@acme.com");

        when(subscriptionService.findById("sub-1")).thenReturn(CompletableFuture.completedFuture(sub));
        when(pricingService.applyDiscount(sub)).thenReturn(discounted);
        when(accountService.findById("acc-1")).thenReturn(CompletableFuture.completedFuture(account));

        EnrichedSubscription result = underTest.enrich("sub-1").get();

        // isEqualByComparingTo equivalent: compareTo returns 0 when values are equal regardless of scale
        assertEquals(0, result.subscription().discountedAmount().compareTo(BigDecimal.valueOf(1080)));
    }

    @Test
    void subscriptionNotFoundCompletesExceptionally() {
        // CF: failedFuture — already-failed CF. Chained thenApply/thenCompose are skipped.
        //     Exception propagates until .exceptionally() or .handle() — Solution has neither,
        //     so the failure propagates to the caller.
        when(subscriptionService.findById("sub-999"))
            .thenReturn(CompletableFuture.failedFuture(
                new RuntimeException("Subscription not found: sub-999")));

        // .get() re-throws wrapped in ExecutionException — CF's cross-thread exception carrier.
        ExecutionException ex = assertThrows(ExecutionException.class,
            () -> underTest.enrich("sub-999").get());
        assertInstanceOf(RuntimeException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("Subscription not found"));
    }
}
