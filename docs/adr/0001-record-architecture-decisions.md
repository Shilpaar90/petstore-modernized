# ADR-0001: Record architecture decisions

## Status
Accepted

## Context
This is a migration exercise that will be presented and questioned. The *reasoning* behind
choices matters as much as the code. In a real large-scale migration, decisions made early are
revisited by many people over months; undocumented rationale becomes tribal knowledge and is
lost.

## Decision
We keep a lightweight decision log using Architecture Decision Records (one file per decision,
Nygard format) under `docs/adr/`. Every non-trivial, hard-to-reverse choice gets an ADR.

## Consequences
- Newcomers (and interviewers) can reconstruct *why*, not just *what*.
- Reversals are explicit and auditable (a superseding ADR).
- Small overhead per decision; negligible compared to the cost of re-litigating choices.
