# Testing Documentation

## Overview

Tests use **JUnit 5**, **Mockito**, and **QuarkusTest** (REST with H2). **JaCoCo** enforces 80% line coverage on the assignment packages (location + warehouse use cases). The full report includes all code; the check applies only to those packages.

## Running Tests

```bash
cd java-assignment

# All tests (unit + QuarkusTest) and JaCoCo check
./mvnw -B verify

# Tests only (report at target/site/jacoco/index.html)
./mvnw test
```

## Test Structure

### Unit tests (positive, negative, error)

| Component | Positive | Negative / Error |
|-----------|----------|-------------------|
| **LocationGatewayTest** | Resolve existing identifier (ZWOLLE-001, AMSTERDAM-001) | Unknown identifier, null, blank |
| **CreateWarehouseUseCaseTest** | Create when all validations pass | Null warehouse; blank bu code/location; null/negative capacity/stock; stock > capacity; duplicate bu code; invalid location; max warehouses reached; capacity exceeded |
| **ReplaceWarehouseUseCaseTest** | Replace when valid (archive + create) | Null; blank bu code; no active warehouse (404); invalid location; new capacity < current stock; stock mismatch |
| **ArchiveWarehouseUseCaseTest** | Archive by id or by business unit code; idempotent when already archived | Null ref; neither id nor code; not found by id; not found by code |

### REST / integration tests (@QuarkusTest, H2 in-memory)

| Test Class | Coverage | Conditions |
|------------|----------|------------|
| **WarehouseResourceQuarkusTest** | Warehouse REST + repository | List (200), create (201/200), get (200/404/400), archive (204/400), replace (200/400) |
| **StoreResourceQuarkusTest** | Store REST | List, get (200/404), create (201/422), update/patch (200/422/404), delete (204/404) |
| **ProductEndpointTest** | Product REST | List, get (200/404), create (201/422), update (200/422/404), delete (204/404) |
| **WarehouseEndpointIT** | Full app (optional) | `@QuarkusIntegrationTest` – list, archive |

## Coverage (JaCoCo – 80% or above)

- **Tool:** JaCoCo Maven plugin (0.8.12).
- **Enforcement:** 80% line coverage per **package** for:
  - `com.fulfilment.application.monolith.location`
  - `com.fulfilment.application.monolith.warehouses.domain.usecases`
- **Report:** Generated for all classes at `target/site/jacoco/index.html`. CI uploads the report as an artifact.

## Best Practices Used

- **Nested classes** for grouping (e.g. positive vs negative/error).
- **Mockito** for ports in use-case tests; **RestAssured** for REST tests.
- **Assertions:** `assertThrows` for expected exceptions, status codes and body in REST tests.
- **H2** in test for QuarkusTests (no PostgreSQL required in CI).
