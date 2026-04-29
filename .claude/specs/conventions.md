# Code Conventions

## Package Structure (per service)
```
com.lab.<project>.<service>/
├── domain/
│   ├── model/          ← Aggregates, Entities, Value Objects
│   ├── event/          ← Domain Events (records)
│   ├── repository/     ← Repository interfaces (no impl here)
│   └── service/        ← Domain Services
├── application/
│   ├── command/        ← Command objects
│   ├── query/          ← Query objects
│   └── service/        ← Application Services (use case orchestrators)
├── infrastructure/
│   ├── persistence/    ← JPA entities, repository impls
│   ├── messaging/      ← Kafka producers/consumers, RabbitMQ
│   ├── cache/          ← Redis clients
│   └── client/         ← External API clients (with ACL)
└── api/
    ├── controller/     ← REST controllers
    ├── dto/            ← Request/Response DTOs (records)
    └── mapper/         ← Domain ↔ DTO mappers
```

## Test Structure (mirrors main)
```
src/test/java/com/lab/<project>/<service>/
├── domain/             ← Unit tests (pure, no Spring context)
├── application/        ← Unit tests with Mockito mocks
├── infrastructure/     ← Integration tests with Testcontainers
└── api/                ← Slice tests (@WebMvcTest)
```

## Maven Module Naming
```xml
<artifactId>search-service</artifactId>
<artifactId>booking-service</artifactId>
<artifactId>common</artifactId>   <!-- shared events, exceptions -->
```

## Domain Event Convention
```java
// Pattern: Domain Event — immutable record of something that happened
// All events carry: eventId (UUID), occurredAt (Instant), aggregateId
public record FlightSearched(
    UUID eventId,
    Instant occurredAt,
    String origin,
    String destination,
    LocalDate departureDate,
    int passengers
) {}
```

## Aggregate Convention
```java
// Pattern: Aggregate Root — enforces invariants, owns its lifecycle
// Rule: never modify aggregate state outside the aggregate itself
public class Booking {
    private BookingId id;
    private BookingStatus status;
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public void confirm() {
        if (status != BookingStatus.PENDING) throw new IllegalStateException("...");
        this.status = BookingStatus.CONFIRMED;
        domainEvents.add(new BookingConfirmed(UUID.randomUUID(), Instant.now(), id.value()));
    }

    public List<DomainEvent> pullDomainEvents() {
        var events = List.copyOf(domainEvents);
        domainEvents.clear();
        return events;
    }
}
```

## Repository Convention
```java
// Pattern: Repository — collection abstraction; only in domain layer as interface
public interface BookingRepository {
    void save(Booking booking);
    Optional<Booking> findById(BookingId id);
}
```

## Application Service Convention
```java
// Pattern: Application Service — orchestrates use case, owns transaction boundary
@Service
@Transactional
public class BookFlightUseCase {
    // inject repository + domain service + event publisher
    public BookingId execute(BookFlightCommand cmd) { ... }
}
```

## REST Controller Convention
```java
// Keep controllers thin — delegate everything to application service
@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {
    @PostMapping
    public ResponseEntity<BookingResponse> book(@Valid @RequestBody BookFlightRequest req) {
        var id = bookFlightUseCase.execute(mapper.toCommand(req));
        return ResponseEntity.created(URI.create("/api/v1/bookings/" + id.value())).body(...);
    }
}
```

## Error Handling
- Domain exceptions: unchecked, extend `DomainException`
- Application exceptions: `NotFoundException`, `ConflictException` (map to 404/409)
- Global handler: `@RestControllerAdvice` in `api` layer
- Never expose stack traces in API responses

## Testing Conventions
```java
// Unit test — no Spring, pure domain logic
class BookingTest {
    @Test
    void should_confirm_pending_booking() {
        var booking = BookingFixture.pending();
        booking.confirm();
        assertThat(booking.status()).isEqualTo(BookingStatus.CONFIRMED);
        assertThat(booking.pullDomainEvents()).hasSize(1).first().isInstanceOf(BookingConfirmed.class);
    }
}

// Integration test — Testcontainers
@SpringBootTest
@Testcontainers
class BookingRepositoryIT {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");
    ...
}
```

## Fixtures / Test Data Builders
- Create `<Entity>Fixture` classes in `src/test/java/.../fixtures/`
- Static factory methods: `BookingFixture.pending()`, `BookingFixture.confirmed()`
- Never duplicate test data setup inline — always use fixtures

## Kafka Topic Naming
```
<project>.<bounded-context>.<event-name>
flight.search.flight-searched
flight.booking.booking-confirmed
flight.booking.booking-failed
flight.payment.payment-charged
```

## Docker Compose Rules
- `depends_on` with `condition: service_healthy` for all infra
- Named volumes, not bind mounts for data
- All ports exposed on localhost for local dev
- One `.env` file per project for configurable values
