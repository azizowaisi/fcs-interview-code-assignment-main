# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**

Yes, I would consider refactoring for consistency and maintainability. The codebase mixes:

- **Store/Product**: Panache entities (active record style) with static methods (listAll, findById) and in-resource persistence.
- **Warehouse**: Repository + domain ports (WarehouseStore, use cases) with a clear separation between domain models, adapters (WarehouseRepository, DbWarehouse), and REST.

I would refactor toward the Warehouse-style approach for Store and Product as well: introduce a repository (or port) layer and use cases so that business rules and persistence are not tied to the REST layer. Benefits: easier testing (mock the store), consistent patterns across the app, and a single place for transaction boundaries and validation. The refactor could be incremental (e.g. add StoreRepository that delegates to Panache under the hood) to limit risk.

2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
**OpenAPI-first (Warehouse):** Pros: contract is explicit and shareable (clients, docs, codegen); server and client can stay in sync; tooling (Swagger UI, mock servers) works out of the box. Cons: extra build step; generated types can be less flexible; contract changes require regenerating and sometimes touching impl.

**Code-first (Store/Product):** Pros: fast to iterate; no codegen; full control over types and signatures. Cons: contract is implicit (only in code); docs and clients can drift; harder to enforce consistency across services.

**Choice:** I would standardize on OpenAPI-first for any API that is (or might be) consumed by other teams or systems, and use code-first only for very small or internal-only endpoints. For this codebase I would add OpenAPI specs for Store and Product and generate resource interfaces (and optionally DTOs) so all three domains follow the same approach and the contract is the single source of truth.

3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
**Priorities:** (1) Unit tests for use cases and validation logic (e.g. CreateWarehouseUseCase, ReplaceWarehouseUseCase) — fast, no DB, high value. (2) Integration tests for REST endpoints (e.g. WarehouseEndpointIT) — confirm HTTP contract and persistence. (3) Repository/adapter tests only where logic is non-trivial (e.g. custom queries).

**Implementation:** Use in-memory or testcontainers for DB in ITs; mock ports (WarehouseStore, LocationResolver) in use-case unit tests so we can assert validation messages and success paths without starting the app. Add a few key negative cases (400/404) in ITs.

**Over time:** Run unit tests on every commit; run ITs in CI before merge. Keep coverage focused on use cases and resources; avoid chasing 100% on DTOs and generated code. When adding features, require at least one unit test for new validation and one IT for new endpoints, so coverage grows with the codebase.
```