package com.interview.algorithms.completable_future.q003_batch_renewal.solution;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolutionTest {

    @Mock PlanService planService;
    @Mock PaymentMethodService paymentMethodService;
    @Mock ChargeService chargeService;

    Solution underTest;

    @BeforeEach
    void setUp() {
        // 10ms timeout — makes timeout test fast; no real waiting in CI
        underTest = new Solution(planService, paymentMethodService, chargeService, 10L);
    }

    @Test
    void allSubscriptionsRenewedSuccessfully() throws Exception {
        stubSuccess("sub-1", "ch-1");
        stubSuccess("sub-2", "ch-2");
        stubSuccess("sub-3", "ch-3");

        List<RenewalResult> results = underTest.processRenewals(List.of("sub-1", "sub-2", "sub-3")).get();

        assertThat(results).hasSize(3);
        assertThat(results).allMatch(RenewalResult::success);
        assertThat(results).extracting(RenewalResult::subscriptionId)
            .containsExactlyInAnyOrder("sub-1", "sub-2", "sub-3");
    }

    @Test
    void oneChargeFailureDoesNotFailEntireBatch() throws Exception {
        // CRITICAL TEST — core requirement of this exercise.
        // The overall CF must complete NORMALLY even when one charge fails.
        stubSuccess("sub-1", "ch-1");

        // sub-2 charge fails (card declined)
        stubPlanAndPayment("sub-2");
        when(chargeService.charge(
                new SubscriptionPlan("sub-2", "MONTHLY", BigDecimal.valueOf(50)),
                new PaymentMethod("sub-2", "CARD", "tok-sub-2")))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Card declined for sub-2")));

        stubSuccess("sub-3", "ch-3");

        // .get() — if overall CF failed this would throw; the fact it doesn't proves isolation
        List<RenewalResult> results = underTest.processRenewals(List.of("sub-1", "sub-2", "sub-3")).get();

        assertThat(results).hasSize(3);
        assertThat(results).filteredOn(r -> "sub-1".equals(r.subscriptionId())).allMatch(RenewalResult::success);
        assertThat(results).filteredOn(r -> "sub-2".equals(r.subscriptionId())).allMatch(r -> !r.success());
        assertThat(results).filteredOn(r -> "sub-2".equals(r.subscriptionId()))
            .extracting(RenewalResult::errorMessage).containsExactly("Card declined for sub-2");
        assertThat(results).filteredOn(r -> "sub-3".equals(r.subscriptionId())).allMatch(RenewalResult::success);
    }

    @Test
    void chargeTimeoutProducesFailedRenewalResult() throws Exception {
        // Technique: stub charge to return a never-completing CF.
        // After 10ms, orTimeout fires → TimeoutException → exceptionally catches it → RenewalResult.failed.
        // extractMessage falls back to class name ("TimeoutException") when getCause().getMessage() == null.
        stubPlanAndPayment("sub-1");
        when(chargeService.charge(
                new SubscriptionPlan("sub-1", "MONTHLY", BigDecimal.valueOf(50)),
                new PaymentMethod("sub-1", "CARD", "tok-sub-1")))
            .thenReturn(new CompletableFuture<>());  // never completes → timeout fires

        List<RenewalResult> results = underTest.processRenewals(List.of("sub-1"))
            .get(2, TimeUnit.SECONDS);  // 2s well above 10ms timeout

        assertThat(results).hasSize(1);
        assertThat(results.get(0).success()).isFalse();
        // TimeoutException has no message → extractMessage returns class simple name
        assertThat(results.get(0).errorMessage()).isEqualTo("TimeoutException");
    }

    @Test
    void resultSizeMatchesInputSize() throws Exception {
        stubSuccess("sub-1", "ch-1");
        stubSuccess("sub-2", "ch-2");

        List<RenewalResult> results = underTest.processRenewals(List.of("sub-1", "sub-2")).get();

        assertThat(results).hasSize(2);
    }

    // ---- helpers ----

    private void stubSuccess(String subscriptionId, String chargeId) {
        stubPlanAndPayment(subscriptionId);
        when(chargeService.charge(
                new SubscriptionPlan(subscriptionId, "MONTHLY", BigDecimal.valueOf(50)),
                new PaymentMethod(subscriptionId, "CARD", "tok-" + subscriptionId)))
            .thenReturn(CompletableFuture.completedFuture(
                new ChargeResult(subscriptionId, chargeId, BigDecimal.valueOf(50))));
    }

    private void stubPlanAndPayment(String subscriptionId) {
        when(planService.getPlan(subscriptionId))
            .thenReturn(CompletableFuture.completedFuture(
                new SubscriptionPlan(subscriptionId, "MONTHLY", BigDecimal.valueOf(50))));
        when(paymentMethodService.getPaymentMethod(subscriptionId))
            .thenReturn(CompletableFuture.completedFuture(
                new PaymentMethod(subscriptionId, "CARD", "tok-" + subscriptionId)));
    }
}
