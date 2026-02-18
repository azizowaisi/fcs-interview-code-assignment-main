# Assignment Compliance Summary

This document maps the submission requirements to the implementation.

## Submission checklist

| Requirement | Status |
|-------------|--------|
| Implement all tasks from CODE_ASSIGNMENT.md | Done (Tasks 1–3; BONUS not implemented) |
| Document unit-testing; JUnit for positive, negative, error | Done – see TESTING.md and tests below |
| Code coverage (JaCoCo) 80% or above | Done – enforced on assignment packages |
| Best practices (quality, standards, exceptions, logging) | Done |
| Case study – challenges and strategies | Done – case-study/CASE_STUDY.md |
| Push to GitHub and share link | You push and share the repo URL |
| CI/CD and health checks (good-to-have) | Done – .github/workflows/ci.yml, /q/health |

## Code assignment tasks (CODE_ASSIGNMENT.md)

| Task | Status | Location |
|------|--------|----------|
| 1. Location – `resolveByIdentifier` | Done | `location/LocationGateway.java` |
| 2. Store – legacy calls after commit | Done | `stores/StoreResource.java` (transaction sync) |
| 3. Warehouse – create, get, replace, archive + validations | Done | `warehouses/` (use cases, repository, REST) |
| BONUS – Product/Warehouse/Store fulfilment | Not implemented | Nice-to-have |

Validations implemented: business unit code uniqueness, location validity, max warehouses per location, capacity/stock rules, replace (capacity accommodation, stock matching).

## Unit testing and JUnit (positive / negative / error)

- **LocationGatewayTest:** positive (existing ids), negative (unknown, null, blank).
- **CreateWarehouseUseCaseTest:** 1 positive, 11 negative/error (null, blank, duplicate code, invalid location, capacity/stock, max warehouses, capacity exceeded).
- **ReplaceWarehouseUseCaseTest:** 1 positive, 6 negative/error (not found, invalid location, capacity, stock mismatch).
- **ArchiveWarehouseUseCaseTest:** positive (by id, by code, idempotent), negative (null, no id/code, not found).
- **REST (QuarkusTest):** WarehouseResourceQuarkusTest, StoreResourceQuarkusTest, ProductEndpointTest – HTTP status and body assertions for success and error (400, 404, 422).

See [TESTING.md](TESTING.md) for structure and how to run tests.

## Code coverage (JaCoCo – 80% or above)

- **Tool:** JaCoCo Maven plugin (0.8.12).
- **Enforcement:** Two PACKAGE-level rules – 80% line coverage each for `location` and `warehouses.domain.usecases`. Build fails if either package is below 80%.
- **Report:** Full report for all classes at `target/site/jacoco/index.html`; CI uploads it as an artifact.

## Best practices

- **Exception handling:** `WarehouseValidationException` (400), `WarehouseNotFoundException` (404); mapped in REST with clear messages.
- **Logging:** `WarehouseResourceImpl` – WARN for validation/not-found, ERROR for inconsistency.
- **Coding standards:** Constructor injection for use cases, validation at boundaries, fail-fast in use cases.

## Case study (CASE_STUDY.md)

All five scenarios have documented challenges and strategies/solutions in [../case-study/CASE_STUDY.md](../case-study/CASE_STUDY.md): cost allocation, cost optimization, financial integration, budgeting/forecasting, warehouse replacement cost control.

## CI/CD and health checks (good-to-have)

- **CI:** `.github/workflows/ci.yml` – build and `mvn verify` on push/PR to main/master; JaCoCo report uploaded as artifact.
- **Health:** `quarkus-smallrye-health` – `/q/health` (liveness/readiness) for probes.

## Push to GitHub and share link

Commit and push your branch, then share the repository URL (e.g. `https://github.com/<your-org>/<repo>`).
