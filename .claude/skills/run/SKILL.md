---
name: run
description: Build, boot, and smoke-test the modernized Pet Store storefront locally. Use when asked to run/start the app, confirm it boots, or manually verify a change in the running storefront. Default profile is embedded H2 (no external services); mongo can also run locally without Docker (see below); postgres genuinely needs Docker.
---

# Run & smoke-test the storefront

## Prerequisites
This build is pinned to Java 21. Always export it first:

```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

## Boot it

Fastest (embedded H2, zero external deps):
```bash
./mvnw spring-boot:run
# or from a built jar:
./mvnw -q package -DskipTests && java -jar target/petstore-modernized-*.jar
```

**Postgres** genuinely needs Docker (Testcontainers-only path) — NOT available on this laptop, so
prefer the test suite / CI for that profile:
```bash
SPRING_PROFILES_ACTIVE=postgres SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/petstore ./mvnw spring-boot:run
```

**Mongo** does NOT need Docker or a system `mongod` — `flapdoodle` (embedded Mongo) is a
test-scoped dependency, so add `-Dspring-boot.run.useTestClasspath=true` to put it on the run
classpath. That activates the same `EmbeddedMongoAutoConfiguration` the test suite uses, which
spins up a real (but ephemeral) local `mongod` and points `spring.data.mongodb.uri` at it
automatically — no `MONGODB_URI` needed:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=mongo -Dspring-boot.run.useTestClasspath=true
```
This is a dev-only trick (data resets every boot — fine, since `CatalogMongoSeeder` only seeds an
empty catalog). It doesn't replace the real Mongo Docker Compose profile from Phase 6 for anything
production-like; it's for quickly eyeballing the Mongo-backed storefront on this laptop.

Run the jar in the background so you can probe it, and wait for health:
```bash
nohup java -jar target/petstore-modernized-*.jar > /tmp/petstore-run.log 2>&1 &
for i in $(seq 1 40); do curl -sf http://localhost:8080/actuator/health >/dev/null 2>&1 && { echo "UP after ${i}s"; break; }; sleep 1; done
```

## Smoke-check
```bash
curl -s http://localhost:8080/actuator/health          # {"status":"UP","groups":["liveness","readiness"]}
curl -s http://localhost:8080/api/catalog/categories   # JSON: BIRDS/CATS/DOGS/FISH/REPTILES
curl -s "http://localhost:8080/api/catalog/categories/FISH?lang=ja_JP"  # i18n: name "魚"
curl -s http://localhost:8080/products/FI-SW-01 | grep -oE "Angelfish|Add to cart"
```
For the full purchase journey (register → login → cart → checkout → OPC seam), run
`./scripts/demo.sh` against the running instance.

## Stop it — safely
Kill only the app, never the user's other processes (e.g. their Chrome). Check what's actually
running first — don't blind-kill by a guessed pattern:
```bash
ps aux | grep -i "petstore\|spring-boot:run" | grep -v grep
```
Then kill the specific PIDs found (a plain jar run is one process; `mvn spring-boot:run` is a
Maven wrapper process plus the `PetstoreApplication` child it spawns — kill both).

## Notes / gotchas
- `/actuator/health`, `/actuator/info`, `/api/catalog/**`, `/`, `/categories/**`, `/products/**`,
  `/items/**`, `/cart/**`, `/register`, `/login` are public; `/account`, `/checkout`, `/orders/**`
  require login.
- Default profile uses in-memory H2 — data resets on restart; the H2 console is at `/h2-console`.
- If `mvn` isn't finding Java 21, the `JAVA_HOME` export above is almost always the fix.
