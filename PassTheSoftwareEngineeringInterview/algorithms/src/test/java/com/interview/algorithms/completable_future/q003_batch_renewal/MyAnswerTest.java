package com.interview.algorithms.completable_future.q003_batch_renewal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyAnswerTest {

    @Mock PlanService planService;
    @Mock PaymentMethodService paymentMethodService;
    @Mock ChargeService chargeService;

    MyAnswer underTest;

    @BeforeEach
    void setUp() {
        underTest = new MyAnswer(planService, paymentMethodService, chargeService);
    }

    @Test
    void allSubscriptionsRenewedSuccessfully() throws Exception {
        stubSuccessfulRenewal("sub-1", "charge-1");
        stubSuccessfulRenewal("sub-2", "charge-2");
        stubSuccessfulRenewal("sub-3", "charge-3");

        List<RenewalResult> results = underTest.processRenewals(List.of("sub-1", "sub-2", "sub-3")).get();

        assertThat(results).hasSize(3);
        assertThat(results).allMatch(RenewalResult::success);
        assertThat(results).extracting(RenewalResult::subscriptionId)
            .containsExactlyInAnyOrder("sub-1", "sub-2", "sub-3");
    }

    @Test
    void oneChargeFailureDoesNotFailEntireBatch() throws Exception {
        stubSuccessfulRenewal("sub-1", "charge-1");
        // sub-2 charge fails
        stubPlanAndPayment("sub-2");
        when(chargeService.charge(
                new SubscriptionPlan("sub-2", "MONTHLY", BigDecimal.valueOf(50)),
                new PaymentMethod("sub-2", "CARD", "token-sub-2")))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Card declined for sub-2")));
        stubSuccessfulRenewal("sub-3", "charge-3");

        List<RenewalResult> results = underTest.processRenewals(List.of("sub-1", "sub-2", "sub-3")).get();

        assertThat(results).hasSize(3); // batch always returns full list
        assertThat(results).filteredOn(r -> r.subscriptionId().equals("sub-1")).allMatch(RenewalResult::success);
        assertThat(results).filteredOn(r -> r.subscriptionId().equals("sub-2")).allMatch(r -> !r.success());
        assertThat(results).filteredOn(r -> r.subscriptionId().equals("sub-2"))
            .extracting(RenewalResult::errorMessage).containsExactly("Card declined for sub-2");
        assertThat(results).filteredOn(r -> r.subscriptionId().equals("sub-3")).allMatch(RenewalResult::success);
    }

    @Test
    void planAndPaymentFetchedInParallelBeforeCharge() throws Exception {
        stubSuccessfulRenewal("sub-1", "charge-1");

        underTest.processRenewals(List.of("sub-1")).get();

        // Both plan and payment method fetched for "sub-1"
        verify(planService).getPlan("sub-1");
        verify(paymentMethodService).getPaymentMethod("sub-1");
        // Charge called with the fetched plan and payment method
        verify(chargeService).charge(
            new SubscriptionPlan("sub-1", "MONTHLY", BigDecimal.valueOf(50)),
            new PaymentMethod("sub-1", "CARD", "token-sub-1"));
    }

    @Test
    void resultListSizeMatchesInputListSize() throws Exception {
        stubSuccessfulRenewal("sub-1", "charge-1");
        stubSuccessfulRenewal("sub-2", "charge-2");

        List<RenewalResult> results = underTest.processRenewals(List.of("sub-1", "sub-2")).get();

        assertThat(results).hasSize(2);
    }

    // ---- helpers ----

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
