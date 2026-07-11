# ADR-0008: Upgrade to Spring Boot 4.1 on Java 21

## Status
Accepted (supersedes [ADR-0002](0002-target-spring-boot-3-5-on-java-21.md))

## Context
[ADR-0002](0002-target-spring-boot-3-5-on-java-21.md) deliberately shipped the migration on the
mature **Spring Boot 3.5.16** line and deferred the 4.x major as a *fast-follow after parity*, to
avoid compounding migration risk with brand-new-framework risk. The brief asks for the **latest
stable** Spring Boot on Java 21; behavioral parity is now established and locked in by the test
suite (57 tests, incl. characterization, security, checkout, and MongoDB/PostgreSQL parity).

## Decision
Upgrade the runtime to **Spring Boot 4.1.0** on Java 21 (Spring Framework 7, Spring Security 7,
Hibernate 7, Jackson 3). The 3.5 state is frozen at the **`spring-boot-3.5`** git tag; `main`
moves forward — a linear, forward-only history, not a fork.

## What the upgrade touched (all edge/config, not domain)
Because the domain and use cases are framework-free (ADR-0003), every change was at the adapters,
configuration, or build — the hexagonal blast-radius bet paying off:

- **Build:** Boot 4 splits its formerly-monolithic auto-config and test slices into per-technology
  modules. Added `spring-boot-starter-flyway`, `spring-boot-h2console`,
  `spring-boot-starter-webmvc-test`, `spring-boot-starter-data-jpa-test`; imported the
  Testcontainers BOM (Boot 4 no longer manages its version); Testcontainers 1.x → 2.x renamed the
  module artifacts (`testcontainers-postgresql`, `testcontainers-junit-jupiter`).
- **Jackson 3:** the Mongo seed reader moved from `com.fasterxml.jackson.databind` to the new
  `tools.jackson.databind` namespace.
- **Relocated auto-config classes:** `PathRequest` and every `spring.autoconfigure.exclude` entry
  moved packages (e.g. `…autoconfigure.jdbc.DataSourceAutoConfiguration` →
  `…jdbc.autoconfigure.DataSourceAutoConfiguration`); the profile excludes were rewritten.
- **Test slices:** `@WebMvcTest`/`@DataJpaTest`/`@AutoConfigureTestDatabase` moved to
  `org.springframework.boot.<tech>.test.autoconfigure` packages. Controller slices now use
  `@AutoConfigureMockMvc(addFilters = false)` instead of importing `SecurityConfig`.
- **Embedded Mongo:** flapdoodle `spring3x` → `spring4x` (the `spring3x` build referenced a
  Boot-3-only `MongoProperties`).
- **Behavioral:** Spring Security 7 now issues a **relative** `/login` redirect for unauthenticated
  access; the two assertions were tightened from a `**/login` pattern to exact `/login`.

## Consequences
- `main` runs the latest stable Spring Boot, satisfying the brief; `spring-boot-3.5` remains a
  runnable, CI-green snapshot for comparison.
- The breadth of module/package churn (but shallow depth — no domain changes) is itself evidence
  for the ports-and-adapters structure.
- CI verifies the upgrade end-to-end, including PostgreSQL (Testcontainers) and the Docker image.
