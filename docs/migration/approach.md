# Migration Approach

How this migration is broken into safely-shippable pieces. The legacy app is small enough to
rewrite in a weekend — but the point of the exercise is to work **as if the codebase were far
larger**, so every choice here is one that scales to a system 100× the size.

## Guiding principles

1. **Understand before you change.** Inventory the deployment topology, component graph, data
   model, and integration seams first (see [as-is-architecture](as-is-architecture.md)). You
   cannot de-risk what you have not mapped.
2. **Always-green trunk.** `main` compiles and passes tests at every commit. Each phase adds a
   vertical slice without breaking what shipped before. CI enforces this from commit #1.
3. **Vertical slices, not horizontal layers.** Migrate one bounded context end-to-end
   (domain → persistence → service → web → tests) before starting the next. This delivers
   demoable value early and localizes risk, versus a big-bang "all entities, then all services."
4. **Lowest-risk-first sequencing.** Start with read-only catalog (no writes, no auth, no
   integrations) to prove the whole pipeline, then climb the risk gradient to cart, identity,
   and finally checkout/orders.
5. **Stable domain, swappable edges (hexagonal).** The domain and use-cases depend on *ports*.
   Framework concerns (JPA, MongoDB, HTTP, security) are *adapters*. This is what makes the
   relational→MongoDB re-platforming a bounded, low-blast-radius change.
6. **Preserve behavior with characterization tests.** Before trusting a migrated query, pin the
   legacy behavior with tests seeded from the original data (`Populate-UTF8.xml`). Parity first,
   improvement second.
7. **Anti-corruption at integration boundaries.** The legacy JMS→OPC hand-off becomes an
   outbound port; the storefront never learns it changed.
8. **Decisions are recorded, not remembered.** Every non-trivial choice is an [ADR](../adr).

## Why these tools scale to a large migration

| Concern (at scale) | Mechanism here |
|--------------------|----------------|
| "Where do we even start?" | Discovery inventory + dependency map + seam analysis |
| Avoiding regressions | Characterization tests + always-green CI |
| Parallelizable work | Bounded-context slices with clear port contracts → independent teams |
| Reversibility / blast radius | Ports-and-adapters; new adapter added alongside old |
| Onboarding & auditability | ADRs, risk register, this document |
| Data correctness | DDL derived from source of truth; parity tests; explicit migration routine |

## Phase plan

Each phase is an independently shippable increment. Phases map 1:1 to the task tracker.

| Phase | Slice | Why here | Exit criteria |
|-------|-------|----------|---------------|
| **0** | Foundation & discovery | Can't migrate what isn't mapped or built | Walking skeleton boots; CI green; discovery + ADRs written |
| **1** | Domain model & data | Everything else depends on the model + schema | Flyway schema from legacy DDL; JPA adapters; seed data; parity tests pass |
| **2** | Catalog (read-only) | Lowest risk; proves domain→persistence→web→test pipeline | Browse/search categories→products→items, i18n, in UI + REST + tests |
| **3** | Identity & cart | Introduces writes + auth in isolation | Spring Security sign-on/registration; session cart |
| **4** | Checkout & orders | Highest-value flow; exercises the OPC seam | Place order end-to-end; `OrderSubmissionPort` stub; confirmation |
| **5** | MongoDB (stretch) | Re-platform DB behind stable ports | `mongo` profile runs full app; data-migration routine; parity tests |
| **6** | Hardening & demo | Ship it | Docker Compose, README/runbook, public repo, demo script |

## Definition of done (per slice)

- Domain logic covered by unit tests; adapter covered by slice tests.
- Behavior parity with legacy verified (characterization tests where data-bound).
- REST + UI reachable; happy path demoable.
- CI green; ADRs updated if a decision was made.

## What we explicitly are *not* doing (and why)

- **Admin / supplier / OPC EARs** — out of scope; storefront is the assignment. OPC is
  represented as a seam.
- **Pixel-faithful JSP recreation** — we rebuild the storefront's *functionality* in Thymeleaf,
  not its exact 2003 markup.
- **Spring Boot 4.x** — deliberately deferred; see
  [ADR-0002](../adr/0002-target-spring-boot-3-5-on-java-21.md). Don't stack a brand-new major
  framework under a migration.
