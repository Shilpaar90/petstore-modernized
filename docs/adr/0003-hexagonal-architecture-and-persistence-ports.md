# ADR-0003: Hexagonal architecture with persistence ports

## Status
Accepted

## Context
The stretch goal requires the app to run against **either** a relational database **or**
MongoDB. Beyond that, migrations live and die on **blast radius**: a change should touch as
little as possible. The legacy app already hints at this — catalog access sits behind a
`CatalogDAO` interface with per-database implementations and a DAO factory.

## Decision
Adopt **ports-and-adapters (hexagonal) architecture**:

- **Domain** — entities and value objects, framework-free (no Spring, no JPA, no Mongo).
- **Application** — use-case `@Service`s that depend only on **ports** (interfaces), e.g.
  `CatalogRepository`, `OrderRepository`, `OrderSubmissionPort`.
- **Adapters (out)** — persistence implementations: a JPA adapter and a MongoDB adapter, each
  implementing the same ports, selected by Spring profile.
- **Adapters (in)** — Spring MVC controllers, Thymeleaf, REST, Spring Security.

Bounded contexts (`catalog`, `cart`, `identity`, `order`) each carry their own
`domain/application/adapter` sub-packages.

## Consequences
- Relational→MongoDB becomes "add an adapter," not "rewrite the app" (see ADR-0004).
- Domain logic is unit-testable without a container or database.
- Slightly more indirection and boilerplate (port interfaces, mappers) than a
  controller→repository-entity CRUD app — an accepted, deliberate cost that pays back at the
  DB swap and in testability.
- Clear contracts let bounded contexts be migrated (or staffed) independently.
