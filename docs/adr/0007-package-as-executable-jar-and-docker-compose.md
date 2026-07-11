# ADR-0007: Package as an executable jar with an optional Docker Compose stack

## Status
Accepted

## Context
The legacy storefront deployed as `petstore.ear` into a full J2EE application server, alongside
three more EARs and container-managed JNDI/JMS/CMP infrastructure. Standing that up is heavy and
opaque. The modernization needs a way to run the app — and to demonstrate the relational→document
re-platform — that is reproducible, requires no hand-configured server, and works on a laptop.

## Decision
- **Executable jar.** Spring Boot packages a single self-contained jar with an embedded servlet
  container (ADR-0005). `java -jar` is the whole deployment; no application server.
- **Container image.** A multi-stage `Dockerfile` builds with the JDK and ships on a slim JRE,
  running as a non-root user, with an Actuator-based `HEALTHCHECK` and a memory-aware JVM flag.
- **Docker Compose for the prod-like run.** `docker-compose.yml` offers two Compose profiles over
  the *same image*: `relational` (app + PostgreSQL) and `document` (app + MongoDB). This makes the
  hexagonal claim tangible — identical application, different persistence edge (ADR-0003/0004).
- **Dev/test needs nothing external.** The default profile uses embedded H2; tests use embedded
  MongoDB. Docker is only for the prod-like demo.
- **Verification.** A Testcontainers test runs the real Flyway migrations and catalog parity
  assertions against actual PostgreSQL in CI (skipped locally when Docker is absent), so the
  Postgres path is exercised, not just asserted.

## Consequences
- One artifact, one command to run; trivial to containerize and orchestrate.
- The two Compose profiles double as the demo of port-swappable persistence.
- CI (with Docker) verifies the relational migration on real Postgres and the whole app on
  embedded Mongo; local `mvn test` stays green with zero external services.
- Secrets/connection details are environment-driven (12-factor), not baked into the image.
