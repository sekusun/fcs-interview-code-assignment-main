# Testing guide

## How to run

- **Unit and integration tests (Surefire)**: `./mvnw test`
- **Package + integration (Failsafe)**: `./mvnw verify` (when the native profile or Failsafe is enabled for your build)

For **`./mvnw test`**, the **`test`** profile uses an **in-memory H2** database (see [`src/test/resources/application.properties`](src/test/resources/application.properties)) so tests do not require Docker. For running the packaged app or `%prod`, use **PostgreSQL** as described in [README.md](README.md) (Docker example on port `15432`).

## Test pyramid (this repo)

| Layer | Annotation / tool | Use for |
|--------|-------------------|---------|
| In-JVM Quarkus | `@QuarkusTest` | REST endpoints with RestAssured, CDI beans, Hibernate Panache, and transactional tests |
| Packaged app | `@QuarkusIntegrationTest` | Smoke tests against the built application (e.g. [WarehouseEndpointIT](src/test/java/com/fulfilment/application/monolith/warehouses/adapters/restapi/WarehouseEndpointIT.java)) |

Prefer **`@QuarkusTest` + RestAssured** for API and persistence-backed behavior during development. Reserve **`@QuarkusIntegrationTest`** for release-style verification.

## Terminology

- **Positive**: Expected success — HTTP **200**, **201**, **204** or domain outcome that matches valid input and business rules.
- **Negative**: Rejected request or rule — typically HTTP **400** (validation / business rule) with a clear message.
- **Error**: Client or conflict conditions — **404** (not found), **409** (conflict), **422** (unprocessable / missing required fields), **500** (unexpected server failure).

## Conventions

- Test classes run by Surefire are named `*Test` under `src/test/java`.
- Prefer assertions on **HTTP status codes** and **structured JSON** where possible; use `containsString` only for quick smoke checks when the response shape is not stable.
- Isolate data when tests mutate the database: use **unique business unit codes**, dedicated entities created inside the test, or transactional test helpers (`@Transactional` on test methods) so tests do not depend on execution order.

## What to prioritize

1. **Domain rules** — warehouse creation/replacement/archive validations and fulfillment cardinality limits.
2. **HTTP adapters** — status codes and error bodies for invalid input.
3. **Smoke / integration** — list endpoints and critical paths (`@QuarkusIntegrationTest`).

For reasoning about trade-offs and long-term test strategy, see question 3 in [QUESTIONS.md](QUESTIONS.md).
