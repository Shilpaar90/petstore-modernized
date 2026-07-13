# ADR-0004: Relational-first, then MongoDB behind the same ports

## Status
Accepted (document-model consequences amended by [ADR-0009](0009-mongo-catalog-schema-access-pattern-review.md))

## Context
The legacy store is relational (Cloudscape/Derby; Oracle DDL also shipped). The stretch goal is
to run against MongoDB. There are two ways to reach a Mongo target: go straight to a document
model, or migrate faithfully to relational first and then re-platform the database.

## Decision
Do **both, in order**:

1. **Faithful relational migration** — JPA entities mapping the shipped schema exactly
   (including the localized `*_details` tables), Flyway migrations derived from `PopulateSQL.xml`,
   original seed data. This is the *correctness baseline*.
2. **MongoDB adapter** — a redesigned aggregate/document model (embed `*_details` as localized
   sub-documents; embed `items` in `product`; embed `lineItems` in `order`) implementing the same
   ports, plus a one-shot relational→document migration routine and parity tests.

Active store chosen by Spring profile (`jpa` default, `mongo` stretch).

## Rationale
- The relational step gives a **known-good oracle** to diff the Mongo results against — you can't
  claim the DB swap is correct without something to compare to.
- It mirrors how real re-platforming projects run: stabilize on a like-for-like target, *then*
  optimize the data model — rather than changing runtime and data model in one leap.
- It makes both stories demoable and directly contrasts relational normalization vs. document
  aggregation — the heart of a MongoDB modernization narrative.

## Consequences
- More total code (two adapters + a migration routine) — accepted; it is the exercise's point.
- Requires disciplined port design so neither adapter leaks storage concerns into the domain.
- The document model is a genuine redesign, not a table-per-collection copy; trade-offs
  (denormalization, update fan-out) are documented in the Phase 5 notes.
