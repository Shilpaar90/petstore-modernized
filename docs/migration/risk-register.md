# Risk Register

Living list of migration risks, ordered by exposure. Reviewed at each phase boundary.
`L` = likelihood, `I` = impact (1 low – 5 high), `E` = L×I exposure.

| # | Risk | L | I | E | Mitigation | Status |
|---|------|---|---|---|------------|--------|
| R1 | **Behavioral regression** — migrated queries/logic silently differ from legacy | 4 | 5 | 20 | Characterization tests seeded from `Populate-UTF8.xml`; parity asserted before/after; always-green CI | Open — mitigated per slice |
| R2 | **i18n `_details` model mishandled** — locale side-tables collapsed incorrectly, wrong prices/names | 3 | 5 | 15 | Model composite `(id, locale)` keys faithfully in JPA; parity tests per locale; document embedding validated against relational in Phase 5 | Open |
| R3 | **MongoDB re-modeling loses relational guarantees** — FKs, uniqueness, referential integrity | 3 | 4 | 12 | Aggregate-oriented document design; app-level invariants in domain; migration routine validates counts/spot-checks vs relational | Open (Phase 5) |
| R4 | **Auth downgrade/upgrade risk** — legacy stored weak/plaintext passwords; parity vs security | 3 | 4 | 12 | Spring Security with BCrypt; treat as an intentional *security upgrade*, not bug-for-bug parity; documented in ADR-0005 | Open |
| R5 | **Scope creep into admin/supplier/OPC** — temptation to migrate back-office | 2 | 4 | 8 | Hard scope boundary; OPC is a seam (ADR-0006); documented non-goals in approach.md | Controlled |
| R6 | **Spring Boot 4.x instability under migration** — brand-new major framework | 2 | 4 | 8 | Target hardened 3.5 line; 3.5→4 as a fast-follow after parity (ADR-0002) | Accepted |
| R7 | **Toolchain drift** — "works on my machine"; JDK/Maven version skew | 3 | 2 | 6 | Pin Java 21; document exact `JAVA_HOME`; Maven wrapper; CI on clean runner | Mitigated |
| R8 | **Order semantics change** — storefront never persisted orders (fire-to-OPC); we add persistence | 2 | 3 | 6 | Explicit ADR; local persistence + submission port models both old async intent and new durability | Documented (ADR-0006) |
| R9 | **Lost domain knowledge** — 2003 code, no original authors | 3 | 2 | 6 | Inventory + as-is doc; DDL is source of truth; small reversible slices | Mitigated |
| R10 | **Demo environment fails live** — network/DB unavailable during playback | 2 | 4 | 8 | Zero-external-dependency default (embedded H2 + embedded Mongo); rehearsed demo script; Docker optional | Planned (Phase 6) |

## Review log

- **Phase 0:** register created. Top exposures R1 (regression) and R2 (i18n) drive the
  characterization-test investment in Phase 1.
