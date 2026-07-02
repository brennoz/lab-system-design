package com.interview.algorithms.completable_future.q001_subscription_chain.solution;

// REFERENCE TESTS — open only after writing your own MyAnswerTest.java
//
// These tests verify Solution.java. Use them as a guide for how to test
// CompletableFuture pipelines with Mockito. Compare your MyAnswerTest to these.

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Mockito: @ExtendWith(MockitoExtension.class) wires Mockito lifecycle into JUnit 5.
//          It initialises @Mock fields before each test and validates mock interactions after.
@ExtendWith(MockitoExtension.class)
class SolutionTest {

    // Mockito: @Mock creates a test double that records all calls and returns null/0/false
    //          by default. We control its behaviour with when(...).thenReturn(...).
    // WHY mocks and not real implementations: we want to test ONLY the CF pipeline logic
    //     in Solution.enrich(). Real service calls would introduce network latency,
    //     require live infrastructure, and make tests non-deterministic.
    @Mock SubscriptionService subscriptionService;
    @Mock PricingService pricingService;
    @Mock AccountService accountService;

    Solution underTest;

    @BeforeEach
    void setUp() {
        // Constructor injection — mocks passed in, no Spring context needed.
        underTest = new Solution(subscriptionService, pricingService, accountService);
    }

    @Test
    void enrichReturnsCombinedSubscriptionAndAccount() throws Exception {
        // ARRANGE: set up stubs for the entire happy-path pipeline
        var sub        = new Subscription("sub-1", "acc-1", "ANNUAL", BigDecimal.valueOf(1200));
        var discounted = new DiscountedSubscription("sub-1", "acc-1", "ANNUAL", BigDecimal.valueOf(1080));
        var account    = new Account("acc-1", "Acme Corp", "billing@acme.com");

        // Mockito stubs: when(...).thenReturn(...)
        // CF: CompletableFuture.completedFuture(value) returns an ALREADY-COMPLETED CF.
        //     This means thenApply/thenCompose callbacks fire synchronously on the calling thread.
        //     No thread pool involved — tests are deterministic and fast.
        when(subscriptionService.findById("sub-1")).thenReturn(CompletableFuture.completedFuture(sub));
        when(pricingService.applyDiscount(sub)).thenReturn(discounted);
        when(accountService.findById("acc-1")).thenReturn(CompletableFuture.completedFuture(account));

        // ACT: .get() blocks until the CF completes; throws ExecutionException on failure.
        // WHY .get() in tests: we need the result synchronously to assert on it.
        //     In production code, never call .get() — chain with .thenApply instead.
        EnrichedSubscription result = underTest.enrich("sub-1").get();

        // ASSERT: result contains discounted subscription (not original) and correct account
        assertThat(result.subscription()).isEqualTo(discounted);
        assertThat(result.account()).isEqualTo(account);
    }

    @Test
    void accountFetchedUsingDiscountedSubscriptionAccountId() throws Exception {
        // This test verifies that the accountId used for the account fetch comes from
        // the DISCOUNTED subscription, not hardcoded or from the input parameter.
        // We use a different accountId ("acc-99") to make this visible.
        var sub        = new Subscription("sub-1", "acc-99", "MONTHLY", BigDecimal.valueOf(100));
        var discounted = new DiscountedSubscription("sub-1", "acc-99", "MONTHLY", BigDecimal.valueOf(90));
        var account    = new Account("acc-99", "Beta Ltd", "pay@beta.com");

        when(subscriptionService.findById("sub-1")).thenReturn(CompletableFuture.completedFuture(sub));
        when(pricingService.applyDiscount(sub)).thenReturn(discounted);
        when(accountService.findById("acc-99")).thenReturn(CompletableFuture.completedFuture(account));

        underTest.enrich("sub-1").get();

        // Mockito: verify() asserts that the mock was called with specific arguments.
        // WHY: if the implementation hardcodes "acc-1" or reads the raw subscription's
        //      accountId instead of the discounted subscription's accountId, this fails.
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

        // AssertJ: isEqualByComparingTo for BigDecimal — avoids scale differences.
        //          new BigDecimal("1080") and BigDecimal.valueOf(1080) are equal by value
        //          but may differ by scale; isEqualTo() would fail, isEqualByComparingTo() passes.
        assertThat(result.subscription().discountedAmount())
            .isEqualByComparingTo(BigDecimal.valueOf(1080));
    }

    @Test
    void subscriptionNotFoundCompletesExceptionally() {
        // CF: CompletableFuture.failedFuture(ex) returns an already-failed CF.
        //     Any .thenApply or .thenCompose chained on a failed CF is skipped.
        //     The exception propagates until it reaches an .exceptionally() or .handle() handler.
        //     Since Solution has no error handler, the failure propagates to the caller.
        when(subscriptionService.findById("sub-999"))
            .thenReturn(CompletableFuture.failedFuture(
                new RuntimeException("Subscription not found: sub-999")));

        // .get() re-throws the exception wrapped in ExecutionException.
        // WHY ExecutionException: CompletableFuture wraps all exceptions this way
        //     to carry them across thread boundaries safely.
        assertThatThrownBy(() -> underTest.enrich("sub-999").get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Subscription not found");
    }
}
