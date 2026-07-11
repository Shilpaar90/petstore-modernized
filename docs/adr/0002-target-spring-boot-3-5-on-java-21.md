# ADR-0002: Target Spring Boot 3.5 on Java 21 (defer 4.x)

## Status
Superseded by [ADR-0008](0008-upgrade-to-spring-boot-4.md) — the 4.x fast-follow was performed
once behavioral parity was established, exactly as this ADR anticipated. The 3.5 milestone is
preserved at the `spring-boot-3.5` git tag.

## Context
The brief asks for "the latest stable version of Spring Boot ... based on Java 21." At the time
of writing, Maven Central lists **Spring Boot 4.1.0** as the newest GA release, with **3.5.16**
as the latest of the mature 3.x line. Spring Boot 4 rides Spring Framework 7 / Spring Security 7,
which carry breaking API changes and only reached GA roughly eight months ago.

We must also stand up Spring Data JPA, Spring Data MongoDB, Spring Security, Thymeleaf, and
Flyway — a broad dependency surface — *while simultaneously* migrating a legacy application.

## Decision
Target **Spring Boot 3.5.16** on **Java 21**. Treat the upgrade to 4.x as a **fast-follow**
performed *after* behavioral parity with the legacy app is established.

## Rationale
- Migrating and adopting a brand-new *major* framework at the same time compounds risk on two
  independent axes. Isolating variables is a core migration discipline.
- 3.5.x is the most hardened current line; every dependency we need has battle-tested support.
- Java 21 (the mandated LTS) is fully supported on 3.5.x, so the runtime requirement is met.
- The hexagonal structure (ADR-0003) keeps framework coupling at the edges, so a later 3.5→4.x
  bump is itself a bounded change.

## Consequences
- We are one major version behind the absolute newest release; called out proactively rather
  than hidden.
- A follow-up ADR will cover the 4.x upgrade once parity tests are green.
- If the reviewer prefers bleeding-edge, switching the parent version is a small, localized change.
