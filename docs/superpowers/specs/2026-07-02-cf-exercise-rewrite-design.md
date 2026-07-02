# CompletableFuture Exercise Rewrite — Design Spec

**Date:** 2026-07-02
**Applies to:** `PassTheSoftwareEngineeringInterview/algorithms/src/main/java/com/interview/algorithms/completable_future/`
**Supersedes:** CF exercise structure design (2026-07-02) and current q001–q003 implementations.

---

## Goal

Rewrite all three CompletableFuture exercises (q001–q003) so they use:
- **Layered / MVC-style architecture** — no hexagonal ports/adapters; concrete stub classes replace abstract interfaces
- **Realistic CF patterns** — `supplyAsync`, virtual thread executor, `completeOnTimeout`, `orTimeout`, `exceptionally`
- **A runnable Main.java** per exercise — demonstrates real parallelism with simulated latency
- **Mockito in SolutionTest.java** — industry-standard test approach expected in Java interviews

Domain stays **Zuora/Zephr billing**. Difficulty stays **simple → medium → medium-hard**.

---

## Decision Log

| Decision | Choice | Reason |
|---|---|---|
| Rewrite or new exercises | Rewrite q001–q003 | Current architecture confuses learner; no value in keeping both |
| Service representation | Concrete stub classes | Visible async behaviour in Main.java; closer to real layered service |
| Test doubles | Mockito `@Mock` | Industry standard; what interviewers expect in Java unit tests |
| Stubs used in | Main.java only | Stubs simulate latency; tests use Mockito mocks for control |
| Main.java location | `solution/` package | Needs access to stub classes; not part of the learner's package |

---

## File Structure Per Exercise

```
q00N_<slug>/
  question.md                    ← problem brief (co-located with source)
  MyAnswer.java                  ← empty class shell — learner starts here

  solution/
    <Model>.java                 ← Java 21 records (Subscription, Invoice, etc.)
    <Service>Stub.java           ← concrete stub; supplyAsync + simulated delay
    Solution.java                ← reference implementation (all CF features, heavy comments)
    Main.java                    ← runnable demo; wires stubs → Solution, prints timings

test/.../q00N_<slug>/solution/
  SolutionTest.java              ← Mockito @Mock; 4 tests; heavily commented
```

---

## Architecture: Layered (not Hexagonal)

### Old approach (hexagonal-style)
```
domain/
  SubscriptionService (interface — port)
  PricingService (interface — port)
infrastructure/
  SubscriptionServiceImpl (adapter)
```
MyAnswer receives interfaces; never sees concrete types.

### New approach (layered/MVC-style)
```
service/
  SubscriptionServiceStub  (concrete class, owns its own executor)
  PricingServiceStub       (concrete class, sync)
  AccountServiceStub       (concrete class, owns its own executor)
service/
  Solution (or MyAnswer)   — "application service" layer; calls other services directly
```
No interfaces. MyAnswer receives concrete service objects via constructor. Straightforward to read, instantiate, and test.

### Stub pattern (for Main.java)
```java
class SubscriptionServiceStub {
    private final ExecutorService executor;
    private final long delayMs;

    SubscriptionServiceStub(ExecutorService executor, long delayMs) {
        this.executor = executor;
        this.delayMs = delayMs;
    }

    // CF: supplyAsync kicks off blocking work on a virtual thread.
    //     The calling thread returns immediately with a CF<Subscription>.
    CompletableFuture<Subscription> findById(String id) {
        return CompletableFuture.supplyAsync(() -> {
            Thread.sleep(delayMs);   // simulate network/DB latency
            return new Subscription(id, "acc-1", "ANNUAL", BigDecimal.valueOf(1200));
        }, executor);
    }
}
```

### Main.java pattern
```java
public class Main {
    public static void main(String[] args) throws Exception {
        // Java 21: newVirtualThreadPerTaskExecutor — one virtual thread per task.
        //          Virtual threads are cheap (no OS thread per task), ideal for I/O-bound work.
        var executor = Executors.newVirtualThreadPerTaskExecutor();

        var solution = new Solution(
            new SubscriptionServiceStub(executor, 200),
            new PricingServiceStub(),             // sync — no executor needed
            new AccountServiceStub(executor, 150)
        );

        long start = System.currentTimeMillis();
        var result = solution.enrich("sub-001").get();
        System.out.printf("Result: %s%nTime: %dms%n", result, System.currentTimeMillis() - start);

        executor.shutdown();
    }
}
```

### Test pattern (Mockito)

Mockito can mock concrete classes (not just interfaces) via objenesis — no interface needed:

```java
@ExtendWith(MockitoExtension.class)
class SolutionTest {
    @Mock SubscriptionServiceStub subscriptionService;
    @Mock PricingServiceStub pricingService;
    @Mock AccountServiceStub accountService;

    Solution underTest;

    @BeforeEach void setUp() {
        // 10ms timeout — makes timeout tests fast and deterministic
        underTest = new Solution(subscriptionService, pricingService, accountService, 10L);
    }

    @Test void enrichReturnsCombinedResult() throws Exception {
        // Stubs are mocked to return completedFuture — no real delay in tests
        when(subscriptionService.findById("sub-1"))
            .thenReturn(CompletableFuture.completedFuture(new Subscription(...)));
        // ... assert on result
    }

    @Test void serviceTimeoutReturnsFallback() throws Exception {
        // Never-completing CF → 10ms timeout fires → fallback value returned
        when(billingAddressService.get("acc-1"))
            .thenReturn(new CompletableFuture<>());  // never completes
        // assert invoice has BillingAddress.unknown()
    }
}
```

---

## CF Features Per Exercise

### q001 — Subscription Chain (Easy)

**CF methods:** `thenApply`, `thenCompose`
**New feature:** Virtual thread executor (visible in stubs)

Stubs return `CF<T>` via `supplyAsync` internally. Solution.java chains:
```java
// Solution.java
public CompletableFuture<EnrichedSubscription> enrich(String subscriptionId) {
    return subscriptionService.findById(subscriptionId)   // CF<Subscription> from stub
        .thenApply(pricingService::applyDiscount)         // sync transform — no IO
        .thenCompose(discounted ->
            accountService.findById(discounted.accountId())  // CF<Account> from stub
                .thenApply(account -> new EnrichedSubscription(discounted, account)));
}
```

`supplyAsync` + `Executors.newVirtualThreadPerTaskExecutor()` visible in `SubscriptionServiceStub.java` and `AccountServiceStub.java` in the same `solution/` package.

Key teaching point: `thenApply` for sync (no IO), `thenCompose` for async (returns new CF, flattens `CF<CF<Account>>`). Virtual thread executor introduced here.

---

### q002 — Invoice Builder (Medium)

**CF methods:** `allOf`, `thenApply` + `join()`, `completeOnTimeout`
**New feature:** `completeOnTimeout` per service (graceful fallback instead of exception)

Stubs return `CF<T>`. Solution.java chains timeout on each:
```java
// Solution.java
public CompletableFuture<Invoice> buildInvoice(String subscriptionId, String accountId) {
    CF<Subscription>   subCf  = subscriptionService.get(subscriptionId)
        .completeOnTimeout(Subscription.empty(), timeoutMs, MILLISECONDS);

    CF<BillingAddress> addrCf = billingAddressService.get(accountId)
        .completeOnTimeout(BillingAddress.unknown(), timeoutMs, MILLISECONDS);

    CF<PaymentMethod>  pmCf   = paymentMethodService.get(accountId)
        .completeOnTimeout(PaymentMethod.unknown(), timeoutMs, MILLISECONDS);

    return CompletableFuture.allOf(subCf, addrCf, pmCf)
        .thenApply(ignored -> new Invoice(subCf.join(), addrCf.join(), pmCf.join()));
}
```

Each model has a static `empty()` / `unknown()` factory for the fallback case. Learner sees distinction: `completeOnTimeout` (returns fallback silently) vs `orTimeout` (throws TimeoutException, introduced in q003).

---

### q003 — Batch Renewal (Medium-Hard)

**CF methods:** `thenCombine`, `thenCompose`, `thenApply`, `exceptionally`, `allOf`, `orTimeout`
**New feature:** `orTimeout` on per-item charge call

Stubs return `CF<T>`. Per-item pipeline in Solution.java:
```java
private CompletableFuture<RenewalResult> renewOne(String subscriptionId) {
    record PlanAndPayment(SubscriptionPlan plan, PaymentMethod pm) {}

    return planService.getPlan(subscriptionId)                   // CF<Plan> from stub
        .thenCombine(pmService.getPaymentMethod(subscriptionId), // CF<PM> from stub — parallel
                     PlanAndPayment::new)
        .thenCompose(pp ->
            chargeService.charge(pp.plan(), pp.pm())            // CF<ChargeResult> from stub
                .orTimeout(timeoutMs, MILLISECONDS))            // hard timeout: TimeoutException if charge hangs
        .thenApply(charge -> RenewalResult.success(subscriptionId, charge))
        .exceptionally(ex -> RenewalResult.failed(subscriptionId, unwrap(ex).getMessage()));
}
```

Key distinction from q002: `orTimeout` throws → caught by `exceptionally` → `RenewalResult.failed(...)`. `completeOnTimeout` (q002) never reaches `exceptionally` — it silently substitutes a value.

---

## Executor Lifecycle

Stubs own the async work — they receive the executor in their constructor and call `supplyAsync` internally. Solution.java never calls `supplyAsync` directly; it chains CFs returned by stubs.

```java
// Main.java creates one executor, passes to all stubs
var executor = Executors.newVirtualThreadPerTaskExecutor();
var solution = new Solution(
    new SubscriptionServiceStub(executor, 200),
    new PricingServiceStub(),           // sync — no executor
    new AccountServiceStub(executor, 150),
    500L                                // timeoutMs — configurable for testability
);
executor.shutdown();
```

```java
// Solution.java — receives stubs, no executor needed
public class Solution {
    private final SubscriptionServiceStub subscriptionService;
    private final PricingServiceStub pricingService;
    private final AccountServiceStub accountService;
    private final long timeoutMs;   // injected — tests pass 10, Main.java passes 500

    public Solution(SubscriptionServiceStub subscriptionService,
                    PricingServiceStub pricingService,
                    AccountServiceStub accountService,
                    long timeoutMs) { ... }
}
```

Stubs return `CompletableFuture<T>`; Solution.java chains with `thenApply`, `thenCompose`, `allOf`, `completeOnTimeout`, `orTimeout`. `supplyAsync` + virtual thread executor are visible inside each `*ServiceStub.java` — part of the `solution/` package the learner reads.

---

## Model Records

Each exercise defines its own records in the `solution/` package. Examples:

**q001:** `Subscription(id, accountId, planName, BigDecimal amount)`, `DiscountedSubscription(id, accountId, planName, BigDecimal discountedAmount)`, `Account(id, name, email)`, `EnrichedSubscription(DiscountedSubscription subscription, Account account)`

**q002:** `Subscription(id, planName, BigDecimal amount)` + `empty()` factory, `BillingAddress(accountId, street, city, postCode)` + `unknown()` factory, `PaymentMethod(accountId, type, last4)` + `unknown()` factory, `Invoice(subscription, billingAddress, paymentMethod)` + `amount()` accessor

**q003:** `SubscriptionPlan(subscriptionId, planName, BigDecimal amount)`, `PaymentMethod(subscriptionId, type, token)`, `ChargeResult(subscriptionId, chargeId, BigDecimal amount)`, `RenewalResult(subscriptionId, boolean success, ChargeResult chargeResult, String errorMessage)` + static factories `success()` / `failed()`

---

## Testing Coverage (SolutionTest.java — 4 tests per exercise)

### q001 tests
1. `enrichReturnsCombinedSubscriptionAndAccount` — happy path
2. `accountFetchedUsingAccountIdFromDiscountedSubscription` — verifies correct accountId used
3. `discountAmountReflectedInResult` — verifies thenApply applied discount
4. `subscriptionNotFoundCompletesExceptionally` — `failedFuture` propagates

### q002 tests
1. `buildInvoicePopulatesAllFields` — happy path
2. `allThreeServicesCalledWithCorrectArguments` — `verify()` on each mock
3. `slowServiceTriggersTimeoutFallback` — mock returns `new CompletableFuture<>()` (never completes); 10ms timeout fires; invoice contains `BillingAddress.unknown()`
4. `allThreeFetchesFireBeforeAnyResultIsUsed` — `AtomicInteger` + unresolved CFs

### q003 tests
1. `allSubscriptionsRenewedSuccessfully` — happy path batch
2. `oneChargeFailureDoesNotFailEntireBatch` — core isolation test
3. `chargeTimeoutProducesFailedRenewalResult` — mock returns never-completing CF; 10ms timeout fires; `exceptionally` catches `TimeoutException`; `RenewalResult.failed`
4. `resultSizeMatchesInputSize` — basic completeness

---

## Scope

- **In scope:** Rewrite q001–q003 main/ and test/ files; delete current versions cleanly
- **Out of scope:** q004+, CLAUDE.md updates, non-CF exercises, HTML lesson changes
