---
name: verify
description: Build and run the full test suite (`./mvnw verify`) to confirm the petstore-modernized project actually compiles and passes tests before considering a change done. Use standalone for a build/test check; the `ship` skill wraps this plus commit/push/CI.
---

# Verify the build

Enforces the project's always-green-trunk rule: every commit must compile and pass tests.

## Run it

```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
./mvnw verify
```

Expected: `BUILD SUCCESS`, `Tests run: N, Failures: 0, Errors: 0, Skipped: 1`.

The 1 skip is `PostgresCatalogParityTest` — it needs Testcontainers + Docker, which this laptop
doesn't have. It runs for real in CI (`Build & test (Java 21)` job). Embedded Mongo tests
(`MongoPortParityTest`, `MongoAppFlowIntegrationTest`) DO run locally — they download `mongod`
on first run, so they need network access.

Anything else failing: stop and fix before proceeding. Don't push or report success on red.

## Faster loop on a single area

To iterate without the full suite:
```bash
./mvnw test -Dtest=CatalogRepositoryJpaAdapterCharacterizationTest
./mvnw test -Dtest='*Mongo*'          # Mongo parity + app-flow tests
./mvnw test -Dtest='*Integration*'    # Auth/Cart/CheckoutFlow integration tests
```
Always finish with a full `./mvnw verify` before calling the work done — a narrowed `-Dtest`
run can't catch cross-module regressions.

## Notes
- If `mvn`/`mvnw` can't find Java 21, the `JAVA_HOME` export above is almost always the fix
  (check with `java -version`).
- This is the build/test half only. To commit, push, and watch CI, use the `ship` skill.
