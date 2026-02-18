# Testing Documentation

## Overview

Tests are written with **JUnit 5** and **Mockito**. Coverage is enforced with **JaCoCo** (minimum 80% line coverage on selected packages). Excluded from coverage: generated API code (`com.warehouse.api`), DTOs/beans, and domain model classes (getters/setters only).

## Running Tests

```bash
# Unit tests only (includes JaCoCo report and check)
./mvnw test

# View coverage report after test
open target/site/jacoco/index.html   # or open the file in browser

# Integration tests (full application)
./mvnw verify
```

## Test Structure

### Unit tests (positive, negative, error)

| Component | Positive | Negative / Error |
|-----------|----------|-------------------|
| **LocationGatewayTest** | Resolve existing identifier (e.g. ZWOLLE-001, AMSTERDAM-001) | Unknown identifier, null, blank |
| **CreateWarehouseUseCaseTest** | Create when all validations pass | Null warehouse; blank bu code/location; null/negative capacity/stock; stock > capacity; duplicate bu code; invalid location; max warehouses reached; capacity exceeded |
| **ReplaceWarehouseUseCaseTest** | Replace when valid (archive + create) | Null; blank bu code; no active warehouse (404); invalid location; new capacity < current stock; stock mismatch |
| **ArchiveWarehouseUseCaseTest** | Archive by id or by business unit code; idempotent when already archived | Null ref; neither id nor code; not found by id; not found by code |

### Integration tests

- **WarehouseEndpointIT** (`@QuarkusIntegrationTest`): List warehouses (200), archive by id (204), list again (archived excluded).
- **ProductEndpointTest**: Product API (as provided).

## Coverage

- **Tool**: JaCoCo Maven plugin (version 0.8.11).
- **Minimum**: 80% line coverage (BUNDLE level).
- **Exclusions**: `com.warehouse.api.**`, `**/beans/**`, `**/domain/models/**`.
- Report path: `target/site/jacoco/index.html`.

## Best Practices Used

- **Nested classes** for grouping (e.g. positive vs negative/error).
- **@DisplayName** for readable test names.
- **Mockito** for ports (WarehouseStore, LocationResolver) in use-case tests.
- **Assertions**: `assertThrows` for expected exceptions, `assertDoesNotThrow` for success, `verify` for mock interactions.
