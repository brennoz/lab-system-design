package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

// REFERENCE TESTS — open only after writing your own MyAnswerTest.java
//
// Key test to understand: oneChargeFailureDoesNotFailEntireBatch.
// This is the critical behaviour of exercise 3 — failure isolation per item.
// Your test suite MUST include a test like this.

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolutionTest {

    @Mock PlanService planService;
    @Mock PaymentMethodService paymentMethodService;
    @Mock ChargeService chargeService;

    Solution underTest;

    @BeforeEach
    void setUp() {
        underTest = new Solution(planService, paymentMethodService, chargeService);
    }

    @Test
    void allSubscriptionsRenewedSuccessfully() throws Exception {
        stubSuccessfulRenewal("sub-1", "charge-1");
        stubSuccessfulRenewal("sub-2", "charge-2");
        stubSuccessfulRenewal("sub-3", "charge-3");

        List<RenewalResult> results = underTest.processRenewals(List.of("sub-1", "sub-2", "sub-3")).get();

        assertThat(results).hasSize(3);
        // AssertJ: allMatch checks a predicate holds for every element in the list.
        assertThat(results).allMatch(RenewalResult::success);
        // containsExactlyInAnyOrder: all three IDs present, order not guaranteed (allOf has no order guarantee).
        assertThat(results).extracting(RenewalResult::subscriptionId)
            .containsExactlyInAnyOrder("sub-1", "sub-2", "sub-3");
    }

    @Test
    void oneChargeFailureDoesNotFailEntireBatch() throws Exception {
        // CRITICAL TEST: this is the core requirement of exercise 3.
        // The returned CF must complete NORMALLY (not exceptionally) even when one charge fails.
        // The failed item appears in the list with success==false.
        stubSuccessfulRenewal("sub-1", "charge-1");

        // sub-2 charge returns a failed CF — simulates card decline
        stubPlanAndPayment("sub-2");
        when(chargeService.charge(
                new SubscriptionPlan("sub-2", "MONTHLY", BigDecimal.valueOf(50)),
                new PaymentMethod("sub-2", "CARD", "token-sub-2")))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Card declined for sub-2")));

        stubSuccessfulRenewal("sub-3", "charge-3");

        // .get() — if the overall CF failed, this would throw ExecutionException.
        // The fact that it doesn't throw proves the failure was isolated to sub-2.
        List<RenewalResult> results = underTest.processRenewals(List.of("sub-1", "sub-2", "sub-3")).get();

        assertThat(results).hasSize(3);  // batch size unchanged
        assertThat(results).filteredOn(r -> r.subscriptionId().equals("sub-1")).allMatch(RenewalResult::success);
        assertThat(results).filteredOn(r -> r.subscriptionId().equals("sub-2")).allMatch(r -> !r.success());
        assertThat(results).filteredOn(r -> r.subscriptionId().equals("sub-2"))
            .extracting(RenewalResult::errorMessage)
            .containsExactly("Card declined for sub-2");
        assertThat(results).filteredOn(r -> r.subscriptionId().equals("sub-3")).allMatch(RenewalResult::success);
    }

    @Test
    void planAndPaymentFetchedBeforeCharge() throws Exception {
        // Verifies the per-item pipeline order: plan + payment THEN charge.
        // Mockito's verify() confirms chargeService received the exact objects from stubs.
        stubSuccessfulRenewal("sub-1", "charge-1");

        underTest.processRenewals(List.of("sub-1")).get();

        verify(planService).getPlan("sub-1");
        verify(paymentMethodService).getPaymentMethod("sub-1");
        verify(chargeService).charge(
            new SubscriptionPlan("sub-1", "MONTHLY", BigDecimal.valueOf(50)),
            new PaymentMethod("sub-1", "CARD", "token-sub-1")
        );
    }

    @Test
    void resultSizeMatchesInputSize() throws Exception {
        stubSuccessfulRenewal("sub-1", "charge-1");
        stubSuccessfulRenewal("sub-2", "charge-2");

        List<RenewalResult> results = underTest.processRenewals(List.of("sub-1", "sub-2")).get();

        assertThat(results).hasSize(2);
    }

    // ---- test helpers ----

    // WHY extract to helper methods: avoids repeating 6 lines of stubbing per test.
    //     DRY test helpers make tests read as intent (WHAT) not mechanics (HOW).
    private void stubSuccessfulRenewal(String subscriptionId, String chargeId) {
        stubPlanAndPayment(subscriptionId);
        when(chargeService.charge(
                new SubscriptionPlan(subscriptionId, "MONTHLY", BigDecimal.valueOf(50)),
                new PaymentMethod(subscriptionId, "CARD", "token-" + subscriptionId)))
            .thenReturn(CompletableFuture.completedFuture(
                new ChargeResult(subscriptionId, chargeId, BigDecimal.valueOf(50))));
    }

    private void stubPlanAndPayment(String subscriptionId) {
        when(planService.getPlan(subscriptionId))
            .thenReturn(CompletableFuture.completedFuture(
                new SubscriptionPlan(subscriptionId, "MONTHLY", BigDecimal.valueOf(50))));
        when(paymentMethodService.getPaymentMethod(subscriptionId))
            .thenReturn(CompletableFuture.completedFuture(
                new PaymentMethod(subscriptionId, "CARD", "token-" + subscriptionId)));
    }
}
