# Target Architecture

The modernized storefront: a single Spring Boot 3.5 process on Java 21, structured as
ports-and-adapters with swappable persistence.

## 1. Runtime topology

```
                ┌─────────────────────────────────────────────┐
   Browser ───► │  Spring Boot process (embedded Tomcat)        │
   REST client  │                                               │
                │  Adapters (in):  Spring MVC controllers        │
                │                  Thymeleaf views · REST API    │
                │                  Spring Security               │
                │  ───────────────────────────────────────────  │
                │  Application:    use-case @Services            │
                │  Domain:         entities + value objects      │
                │                  (framework-free)              │
                │  Ports:          Repository / OrderSubmission  │
                │  ───────────────────────────────────────────  │
                │  Adapters (out): ┌── JPA adapter  (profile jpa)│──► H2 / Postgres
                │                  └── Mongo adapter (profile mongo)──► MongoDB
                │                  OrderSubmission adapter        │──► (log/no-op; was JMS→OPC)
                └─────────────────────────────────────────────┘
```

One deployable replaces four EARs + the WAF + the EJB container. Container-managed concerns
(transactions, security, JNDI, lifecycle) become Spring concerns (`@Transactional`, Spring
Security, DI, auto-configuration).

## 2. Bounded contexts

| Context   | Responsibility                                | Legacy origin |
|-----------|-----------------------------------------------|---------------|
| `catalog` | Browse/search categories, products, items; i18n | `catalog` component + DAOs |
| `cart`    | Session shopping cart                          | `cart` component |
| `identity`| Sign-on, registration, account/profile         | `signon` + `customer`/`account` |
| `order`   | Checkout, order + line items, submission seam   | `purchaseorder`/`lineitem` + `asyncsender` |

Each context is a package with `domain/`, `application/` (services + ports), and
`adapter/{in/web, out/persistence}`.

## 3. Persistence strategy

Two adapters implement the **same** repository ports; the active one is chosen by Spring profile.

### Relational (default, `jpa`)
- JPA/Hibernate entities mapping the legacy schema faithfully, including the localized
  `*_details` tables (composite keys on `(id, locale)`).
- **Flyway** migrations derived from the shipped `PopulateSQL.xml` DDL — schema as versioned code.
- H2 in-memory for dev/test; Postgres for a prod-like run.

### Document (stretch, `mongo`)
- Redesigned for MongoDB's strengths: the `_details` side-tables **embed** as localized
  sub-documents; a `product` embeds its `items`; an `order` embeds its `lineItems`. Fewer
  joins, aggregate-oriented documents.
- Spring Data MongoDB repositories; embedded Mongo for tests, Atlas or Docker for runtime.
- A one-shot migration routine reads relational aggregates and writes documents.

See [ADR-0003](../adr/0003-hexagonal-architecture-and-persistence-ports.md) and
[ADR-0004](../adr/0004-relational-first-then-mongodb.md).

## 4. Web & security

- **Spring MVC + Thymeleaf** for the storefront pages; a parallel **REST API** under `/api`.
- **Spring Security** replaces the `SignOnFilter` + CMP `User` entity: form login, BCrypt-hashed
  passwords (an upgrade from the legacy plaintext/weakly-hashed store), method/route authorization.

## 5. Integration seam (OPC)

Checkout persists the order locally and calls `OrderSubmissionPort.submit(order)`. The default
adapter logs/records the submission (standing in for the legacy JMS publish to the OPC). Swapping
in a real Kafka/JMS/HTTP adapter later is a one-class change — the storefront is unaffected.
See [ADR-0006](../adr/0006-order-submission-port-replaces-jms-opc.md).

## 6. Cross-cutting

- **Config:** `application.yml` + profile-specific overrides; 12-factor env vars for secrets.
- **Observability:** Spring Boot Actuator (`/actuator/health`, `/info`, metrics).
- **Testing:** unit (domain), slice (`@WebMvcTest`/`@DataJpaTest`), characterization (parity),
  context smoke test.
- **Packaging:** executable jar; optional Dockerfile + Compose (app + Postgres + MongoDB).
