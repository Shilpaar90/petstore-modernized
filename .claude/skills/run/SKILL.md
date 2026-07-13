---
name: run
description: Build, boot, and smoke-test the modernized Pet Store storefront locally. Use when asked to run/start the app, confirm it boots, or manually verify a change in the running storefront. Default profile is embedded H2 (no external services); the postgres/mongo profiles need Docker.
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

Prod-like profiles (need Docker — NOT available on this laptop, so prefer the test
suite / CI for these):
```bash
# relational on Postgres
SPRING_PROFILES_ACTIVE=postgres SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/petstore ./mvnw spring-boot:run
# document store on Mongo
SPRING_PROFILES_ACTIVE=mongo MONGODB_URI=mongodb://localhost:27017/petstore ./mvnw spring-boot:run
```

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
Kill only the app, never the user's other processes (e.g. their Chrome):
```bash
pkill -f "petstore-modernized-.*\.jar"      # or the spring-boot:run process
```

## Notes / gotchas
- `/actuator/health`, `/actuator/info`, `/api/catalog/**`, `/`, `/categories/**`, `/products/**`,
  `/items/**`, `/cart/**`, `/register`, `/login` are public; `/account`, `/checkout`, `/orders/**`
  require login.
- Default profile uses in-memory H2 — data resets on restart; the H2 console is at `/h2-console`.
- If `mvn` isn't finding Java 21, the `JAVA_HOME` export above is almost always the fix.
