# CompletableFuture Exercise Rewrite — Design Spec

**Date:** 2026-07-02
**Applies to:** `PassTheSoftwareEngineeringInterview/algorithms/src/main/java/com/interview/algorithms/completable_future/`
**Supersedes:** CF exercise structure design (2026-07-02) and current q001–q003 implementations.

---

## Goal

Rewrite all three CompletableFuture exercises (q001–q003) so they use:
- **Standard layered / Spring Boot-style architecture** — service interfaces + `*Impl` concrete classes; no hexagonal ports/adapters
- **Realistic CF patterns** — `supplyAsync`, virtual thread executor, `completeOnTimeout`, `orTimeout`, `exceptionally`
- **A runnable Main.java** per exercise — demonstrates real parallelism with simulated latency
- **Mockito in SolutionTest.java** — industry-standard test approach expected in Java interviews

Domain stays **Zuora/Zephr billing**. Difficulty stays **simple → medium → medium-hard**.

---

## Decision Log

| Decision | Choice | Reason |
|---|---|---|
| Rewrite or new exercises | Rewrite q001–q003 | Current architecture confuses learner; no value in keeping both |
| Service representation | Interface + `*Impl` concrete class | Standard Spring Boot pattern; what interviewers expect |
| `*Impl` naming | `SubscriptionServiceImpl` (not Stub) | Realistic; comment clarifies it simulates latency |
| Test doubles | Mockito `@Mock` on interface | Industry standard; cleaner proxy than mocking concrete class |
| `*Impl` used in | Main.java only | Impls simulate latency; tests mock the interface |
| Main.java location | `solution/` package | Needs access to `*Impl` classes; not part of learner's package |
| Timeout config | `timeoutMs` constructor param | Tests pass 10ms (fast); Main.java passes 500ms |

---

## File Structure Per Exercise

```
q00N_<slug>/
  question.md                      ← problem brief (co-located with source)
  MyAnswer.java                    ← empty class shell — learner starts here

  solution/
    <Model>.java                   ← Java 21 records (Subscription, Invoice, etc.)
    <Service>.java                 ← interface (3 lines — standard Spring Boot contract)
    <Service>Impl.java             ← concrete impl; supplyAsync + simulated delay
    Solution.java                  ← reference implementation (all CF features, heavy comments)
    Main.java                      ← runnable demo; wires Impls → Solution, prints timings

test/.../q00N_<slug>/solution/
  SolutionTest.java                ← Mockito @Mock on interfaces; 4 tests; heavily commented
```

---

## Architecture: Layered / Spring Boot Style

### Old approach (hexagonal-style, confusing for learner)
```
domain/
  SubscriptionService  (interface — abstract port)
infrastructure/
  SubscriptionServiceAdapter  (maps domain ↔ external system)
```
Abstract, verbose, unclear which layer owns what.

### New approach (standard layered / Spring Boot)
```
// Contract — 3 lines, just like a Spring Boot @Service interface
interface SubscriptionService {
    CompletableFuture<Subscription> findById(String id);
}

// Implementation — what Spring Boot would @Autowire in production
// In our exercises: simulates HTTP/DB latency with Thread.sleep
class SubscriptionServiceImpl implements SubscriptionService {
    private final ExecutorService executor;
    private final long delayMs;

    // In production: Spring would inject real dependencies (RestTemplate, JdbcTemplate, etc.)
    SubscriptionServiceImpl(ExecutorService executor, long delayMs) {
        this.executor = executor;
        this.delayMs = delayMs;
    }

    @Override
    public CompletableFuture<Subscription> findById(String id) {
        // CF: supplyAsync — submits blocking work to the executor, returns CF immediately.
        // Java 21: virtual thread executor — one lightweight virtual thread per task, no OS thread blocking.
        // In production: replace Thread.sleep with real HTTP call or JPA query.
        return CompletableFuture.supplyAsync(() -> {
            Thread.sleep(delayMs);   // simulate network/DB latency
            return new Subscription(id, "acc-1", "ANNUAL", BigDecimal.valueOf(1200));
        }, executor);
    }
}
```

`Solution.java` receives interfaces (same as MyAnswer would in a Spring Boot context):
```java
public class Solution {
    private final SubscriptionService subscriptionService;
    private final PricingService pricingService;
    private final AccountService accountService;
    private final long timeoutMs;

    // Spring Boot: @Autowired constructor injection — interfaces injected, impl chosen by container
    public Solution(SubscriptionService subscriptionService,
                    PricingService pricingService,
                    AccountService accountService,
                    long timeoutMs) { ... }
}
```

### Main.java pattern
```java
public class Main {
    public static void main(String[] args) throws Exception {
        // Java 21: newVirtualThreadPerTaskExecutor — one virtual thread per task.
        //          Cheap to create, ideal for I/O-bound work (HTTP calls, DB queries).
        //          Never blocks an OS thread waiting for I/O.
        var executor = Executors.newVirtualThreadPerTaskExecutor();

        var solution = new Solution(
            new SubscriptionServiceImpl(executor, 200),   // simulates 200ms network call
            new PricingServiceImpl(),                     // sync — no executor needed
            new AccountServiceImpl(executor, 150),        // simulates 150ms network call
            500L                                          // 500ms timeout for Main.java
        );

        long start = System.currentTimeMillis();
        var result = solution.enrich("sub-001").get();
        System.out.printf("Result: %s%nTime: %dms%n", result, System.currentTimeMillis() - start);

        executor.shutdown();
    }
}
```

### Test pattern (Mockito on interfaces)
```java
@ExtendWith(MockitoExtension.class)
class SolutionTest {
    // Mockito mocks the INTERFACE — clean JDK proxy, no objenesis needed
    @Mock SubscriptionService subscriptionService;
    @Mock PricingService pricingService;
    @Mock AccountService accountService;

    Solution underTest;

    @BeforeEach
    void setUp() {
        // 10ms timeout — makes timeout tests fast and deterministic in CI
        underTest = new Solution(subscriptionService, pricingService, accountService, 10L);
    }

    @Test
    void enrichReturnsCombinedResult() throws Exception {
        when(subscriptionService.findById("sub-1"))
            .thenReturn(CompletableFuture.completedFuture(new Subscription(...)));
        // ... assert result
    }

    @Test
    void slowServiceTriggersTimeoutFallback() throws Exception {
        // Never-completing CF → 10ms timeout fires → fallback value used
        when(billingAddressService.get("acc-1"))
            .thenReturn(new CompletableFuture<>());   // never completes
        // assert invoice contains BillingAddress.unknown()
    }
}
```

---

## CF Features Per Exercise

### q001 — Subscription Chain (Easy)

**CF methods:** `thenApply`, `thenCompose`
**New feature:** Virtual thread executor (visible in `*Impl` classes)

Impls return `CF<T>` via `supplyAsync`. Solution.java chains:
```java
public CompletableFuture<EnrichedSubscription> enrich(String subscriptionId) {
    return subscriptionService.findById(subscriptionId)    // CF<Subscription> from impl
        .thenApply(pricingService::applyDiscount)          // sync transform — no IO, no executor
        .thenCompose(discounted ->
            accountService.findById(discounted.accountId())   // CF<Account> from impl
                .thenApply(account -> new EnrichedSubscription(discounted, account)));
}
```

Key teaching point: `thenApply` for sync (no IO), `thenCompose` for async (flattens `CF<CF<Account>>`). Virtual thread executor introduced here and carried forward.

---

### q002 — Invoice Builder (Medium)

**CF methods:** `allOf`, `thenApply` + `join()`, `completeOnTimeout`
**New feature:** `completeOnTimeout` per service (graceful fallback instead of exception)

```java
public CompletableFuture<Invoice> buildInvoice(String subscriptionId, String accountId) {
    // All three fire simultaneously — independent, no result depends on another
    CF<Subscription>   subCf  = subscriptionService.get(subscriptionId)
        .completeOnTimeout(Subscription.empty(), timeoutMs, MILLISECONDS);   // fallback on slow service

    CF<BillingAddress> addrCf = billingAddressService.get(accountId)
        .completeOnTimeout(BillingAddress.unknown(), timeoutMs, MILLISECONDS);

    CF<PaymentMethod>  pmCf   = paymentMethodService.get(accountId)
        .completeOnTimeout(PaymentMethod.unknown(), timeoutMs, MILLISECONDS);

    return CompletableFuture.allOf(subCf, addrCf, pmCf)
        .thenApply(ignored -> new Invoice(subCf.join(), addrCf.join(), pmCf.join()));
}
```

Each model has a static `empty()` / `unknown()` factory. Key distinction taught: `completeOnTimeout` (returns fallback silently, CF completes normally) vs `orTimeout` (throws `TimeoutException`, introduced in q003).

---

### q003 — Batch Renewal (Medium-Hard)

**CF methods:** `thenCombine`, `thenCompose`, `thenApply`, `exceptionally`, `allOf`, `orTimeout`
**New feature:** `orTimeout` on per-item charge call

```java
private CompletableFuture<RenewalResult> renewOne(String subscriptionId) {
    record PlanAndPayment(SubscriptionPlan plan, PaymentMethod pm) {}

    return planService.getPlan(subscriptionId)                    // CF<Plan> — async
        .thenCombine(pmService.getPaymentMethod(subscriptionId),  // CF<PM>  — parallel, independent
                     PlanAndPayment::new)
        .thenCompose(pp ->
            chargeService.charge(pp.plan(), pp.pm())             // CF<ChargeResult> — depends on both
                .orTimeout(timeoutMs, MILLISECONDS))             // hard timeout — TimeoutException if charge hangs
        .thenApply(charge -> RenewalResult.success(subscriptionId, charge))
        .exceptionally(ex -> RenewalResult.failed(subscriptionId, unwrap(ex).getMessage()));
        // exceptionally catches BOTH card decline AND timeout — both become RenewalResult.failed
}
```

Key distinctions:
- `orTimeout` (q003) throws → `exceptionally` catches → `RenewalResult.failed`
- `completeOnTimeout` (q002) silently substitutes fallback — never reaches `exceptionally`
- Per-item `exceptionally` isolates failure: one charge failure does NOT fail the whole batch

---

## Executor Lifecycle

Impls own the `supplyAsync` call. Solution.java never calls `supplyAsync` — it only chains CFs:

```
Main.java                    Solution.java              *ServiceImpl.java
───────────────────────────────────────────────────────────────────────
creates executor        →    receives interface     →   owns supplyAsync
passes to *Impl              chains CFs                 simulates latency
shuts down executor          no executor reference       returns CF<T>
```

`supplyAsync` + virtual thread executor are visible in each `*ServiceImpl.java` in `solution/` — that's the teaching artifact. Solution.java stays clean: just CF chaining.

---

## Model Records

**q001:** `Subscription(id, accountId, planName, BigDecimal amount)` · `DiscountedSubscription(id, accountId, planName, BigDecimal discountedAmount)` · `Account(id, name, email)` · `EnrichedSubscription(DiscountedSubscription, Account)`

**q002:** `Subscription(id, planName, BigDecimal amount)` + `empty()` · `BillingAddress(accountId, street, city, postCode)` + `unknown()` · `PaymentMethod(accountId, type, last4)` + `unknown()` · `Invoice(subscription, billingAddress, paymentMethod)` + `amount()` accessor

**q003:** `SubscriptionPlan(subscriptionId, planName, BigDecimal amount)` · `PaymentMethod(subscriptionId, type, token)` · `ChargeResult(subscriptionId, chargeId, BigDecimal amount)` · `RenewalResult(subscriptionId, boolean success, ChargeResult, String errorMessage)` + `success()` / `failed()` static factories

---

## Testing Coverage (SolutionTest.java — 4 tests per exercise)

### q001 tests
1. `enrichReturnsCombinedSubscriptionAndAccount` — happy path
2. `accountFetchedUsingAccountIdFromDiscountedSubscription` — verifies correct accountId passed to accountService
3. `discountAmountReflectedInResult` — verifies thenApply applied discount correctly
4. `subscriptionNotFoundCompletesExceptionally` — `failedFuture` propagates to caller

### q002 tests
1. `buildInvoicePopulatesAllFields` — happy path, all three services return values
2. `allThreeServicesCalledWithCorrectArguments` — `verify()` on each mock with correct IDs
3. `slowServiceTriggersTimeoutFallback` — mock returns `new CompletableFuture<>()` (never completes); 10ms timeout fires; invoice contains `BillingAddress.unknown()`
4. `allThreeFetchesFireBeforeAnyResultIsUsed` — `AtomicInteger` + unresolved CFs prove parallel execution

### q003 tests
1. `allSubscriptionsRenewedSuccessfully` — happy path batch, all succeed
2. `oneChargeFailureDoesNotFailEntireBatch` — one `failedFuture` → `RenewalResult.failed`; rest succeed; overall CF completes normally
3. `chargeTimeoutProducesFailedRenewalResult` — mock returns `new CompletableFuture<>()` (never resolves); 10ms `orTimeout` fires; `exceptionally` catches `TimeoutException`; `RenewalResult.failed`
4. `resultSizeMatchesInputSize` — output list size equals input list size

---

## Scope

- **In scope:** Rewrite q001–q003 main/ and test/ files; delete current solution/ content; replace with new interface + Impl + Solution + Main pattern
- **Out of scope:** q004+, CLAUDE.md updates, non-CF exercises, HTML lesson changes
