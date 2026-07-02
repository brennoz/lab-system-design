# CF Tests: JUnit 5 Assertions + Double-Safety Timeout Pattern — Design Spec

**Date:** 2026-07-02
**Applies to:** `PassTheSoftwareEngineeringInterview/algorithms/src/test/java/.../completable_future/` (q001–q003 SolutionTests) and `q002_invoice_builder/solution/Solution.java`

---

## Goal

Two targeted changes to the CompletableFuture exercises:

1. **Replace AssertJ with JUnit 5 assertions** in all three `SolutionTest.java` files (q001, q002, q003)
2. **Add `completeOnTimeout().exceptionally()` double-safety chain** to `q002_invoice_builder/solution/Solution.java`, plus a matching 5th test

---

## Decision Log

| Decision | Choice | Reason |
|---|---|---|
| Test assertion library | JUnit 5 (`org.junit.jupiter.api.Assertions`) | More representative of what's tested in interviews; reduces dependency on AssertJ in exercises |
| BigDecimal comparison | `assertEquals(0, x.compareTo(y))` | `assertEquals(BigDecimal, BigDecimal)` fails on scale differences; `compareTo` is scale-safe |
| q003 filtered assertions | Explicit `stream().filter().findFirst().orElseThrow()` + individual asserts | More verbose than AssertJ but clearer to read without library knowledge |
| New CF pattern location | q002 (Invoice Builder) | Already uses `completeOnTimeout` — adds `exceptionally` as a complementary second safety net |
| New test count (q002) | 5 tests | A 5th test is justified because `exceptionally` is a genuinely distinct failure path from the timeout path |
| `exceptionally` placement | Chained after `completeOnTimeout` on each service CF | Handles broken services (exception thrown) separately from slow services (timeout) |

---

## Change 1: AssertJ → JUnit 5 (all 3 SolutionTests)

### Import changes

Remove:
```java
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
```

Add:
```java
import static org.junit.jupiter.api.Assertions.*;
```

### Assertion translation table

| AssertJ | JUnit 5 |
|---|---|
| `assertThat(x).isEqualTo(y)` | `assertEquals(y, x)` |
| `assertThat(x).isEqualByComparingTo(BigDecimal.valueOf(n))` | `assertEquals(0, x.compareTo(BigDecimal.valueOf(n)))` |
| `assertThat(x).isTrue()` | `assertTrue(x)` |
| `assertThat(x).isFalse()` | `assertFalse(x)` |
| `assertThat(x).isNotNull()` | `assertNotNull(x)` |
| `assertThat(list).hasSize(n)` | `assertEquals(n, list.size())` |
| `assertThat(list).allMatch(pred)` | `assertTrue(list.stream().allMatch(pred))` |
| `assertThat(x).as("msg").isEqualTo(y)` | `assertEquals(y, x, "msg")` |
| `assertThatThrownBy(() -> ...).isInstanceOf(X).hasCauseInstanceOf(Y).hasMessageContaining(z)` | `var ex = assertThrows(X.class, () -> ...); assertInstanceOf(Y.class, ex.getCause()); assertTrue(ex.getCause().getMessage().contains(z))` |

### q003 `filteredOn` → explicit stream (Test 2: `oneChargeFailureDoesNotFailEntireBatch`)

AssertJ version:
```java
assertThat(results).filteredOn(r -> "sub-1".equals(r.subscriptionId())).allMatch(RenewalResult::success);
assertThat(results).filteredOn(r -> "sub-2".equals(r.subscriptionId())).allMatch(r -> !r.success());
assertThat(results).filteredOn(r -> "sub-2".equals(r.subscriptionId()))
    .extracting(RenewalResult::errorMessage).containsExactly("Card declined for sub-2");
assertThat(results).filteredOn(r -> "sub-3".equals(r.subscriptionId())).allMatch(RenewalResult::success);
```

JUnit 5 replacement:
```java
var sub1 = results.stream().filter(r -> "sub-1".equals(r.subscriptionId())).findFirst().orElseThrow();
var sub2 = results.stream().filter(r -> "sub-2".equals(r.subscriptionId())).findFirst().orElseThrow();
var sub3 = results.stream().filter(r -> "sub-3".equals(r.subscriptionId())).findFirst().orElseThrow();
assertTrue(sub1.success());
assertFalse(sub2.success());
assertEquals("Card declined for sub-2", sub2.errorMessage());
assertTrue(sub3.success());
```

### q003 `extracting + containsExactlyInAnyOrder` → explicit set check (Test 1: `allSubscriptionsRenewedSuccessfully`)

AssertJ version:
```java
assertThat(results).extracting(RenewalResult::subscriptionId)
    .containsExactlyInAnyOrder("sub-1", "sub-2", "sub-3");
```

JUnit 5 replacement:
```java
var ids = results.stream().map(RenewalResult::subscriptionId).toList();
assertTrue(ids.containsAll(List.of("sub-1", "sub-2", "sub-3")));
```

### q001 `assertThatThrownBy` chain → `assertThrows` (Test 4: `subscriptionNotFoundCompletesExceptionally`)

AssertJ version:
```java
assertThatThrownBy(() -> underTest.enrich("sub-999").get())
    .isInstanceOf(ExecutionException.class)
    .hasCauseInstanceOf(RuntimeException.class)
    .hasMessageContaining("Subscription not found");
```

JUnit 5 replacement:
```java
ExecutionException ex = assertThrows(ExecutionException.class,
    () -> underTest.enrich("sub-999").get());
assertInstanceOf(RuntimeException.class, ex.getCause());
assertTrue(ex.getCause().getMessage().contains("Subscription not found"));
```

---

## Change 2: Double-Safety Chain in q002 Solution.java

### Teaching concept

`completeOnTimeout` and `exceptionally` handle different failure modes:

- `completeOnTimeout(fallback, t, UNIT)` — fires when the CF is **still pending** after `t` ms. Does NOT catch exceptions already thrown. The CF completes normally with the fallback.
- `exceptionally(ex -> fallback)` — fires when the CF **completed exceptionally** (service threw an error, network failure, etc.). Does NOT handle timeouts by itself.

Chained together:
```java
.completeOnTimeout(Subscription.empty(), timeoutMs, MILLISECONDS)
.exceptionally(ex -> Subscription.empty())
```

This means:
- Slow service (pending at timeout) → `completeOnTimeout` fires, returns `empty()`
- Broken service (throws exception) → `exceptionally` fires, returns `empty()`
- Either way: `allOf` always completes normally and the invoice always builds

### Code change (same pattern applied to all 3 service CFs)

```java
// Before
CompletableFuture<Subscription> subCf = subscriptionService.getSubscription(subscriptionId)
    .completeOnTimeout(Subscription.empty(), timeoutMs, MILLISECONDS);

// After
CompletableFuture<Subscription> subCf = subscriptionService.getSubscription(subscriptionId)
    .completeOnTimeout(Subscription.empty(), timeoutMs, MILLISECONDS)
    .exceptionally(ex -> Subscription.empty());

CompletableFuture<BillingAddress> addrCf = billingAddressService.getBillingAddress(accountId)
    .completeOnTimeout(BillingAddress.unknown(), timeoutMs, MILLISECONDS)
    .exceptionally(ex -> BillingAddress.unknown());

CompletableFuture<PaymentMethod> pmCf = paymentMethodService.getDefaultPaymentMethod(accountId)
    .completeOnTimeout(PaymentMethod.unknown(), timeoutMs, MILLISECONDS)
    .exceptionally(ex -> PaymentMethod.unknown());
```

Comment to add above the chain:
```java
// WHY both completeOnTimeout AND exceptionally:
//     completeOnTimeout handles SLOW services (CF still pending after timeoutMs)
//     exceptionally    handles BROKEN services (CF completed with an exception)
//     Together: allOf() always completes normally regardless of how each service fails.
```

### New test in q002 SolutionTest (Test 5)

```java
@Test
void failingServiceTriggersExceptionallyFallback() throws Exception {
    // completeOnTimeout handles slow services; exceptionally handles broken services.
    // Stub getBillingAddress to return failedFuture — simulates a network error, not a timeout.
    var sub = new Subscription("sub-1", "ANNUAL", BigDecimal.valueOf(1200));
    var pm  = new PaymentMethod("acc-1", "CARD", "4242");

    when(subscriptionService.getSubscription("sub-1")).thenReturn(CompletableFuture.completedFuture(sub));
    when(billingAddressService.getBillingAddress("acc-1"))
        .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Address service unavailable")));
    when(paymentMethodService.getDefaultPaymentMethod("acc-1")).thenReturn(CompletableFuture.completedFuture(pm));

    Invoice invoice = underTest.buildInvoice("sub-1", "acc-1").get(2, TimeUnit.SECONDS);

    // exceptionally caught the broken service — fallback used, invoice built normally
    assertEquals(BillingAddress.unknown(), invoice.billingAddress());
    assertEquals(sub, invoice.subscription());
    assertEquals(pm, invoice.paymentMethod());
}
```

---

## Scope

- **In scope:** q001 SolutionTest (AssertJ → JUnit 5), q002 SolutionTest (AssertJ → JUnit 5 + new Test 5), q003 SolutionTest (AssertJ → JUnit 5), q002 Solution.java (`exceptionally` added after each `completeOnTimeout`)
- **Out of scope:** MyAnswer.java files, question.md files, q004+, Solution.java for q001/q003
