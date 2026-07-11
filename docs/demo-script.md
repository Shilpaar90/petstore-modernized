# Demo script

A five-minute walkthrough that shows the migrated storefront working end-to-end and highlights the
migration decisions behind each step. Pairs with the automated `scripts/demo.sh`.

## Setup

```bash
# Simplest (embedded H2, no external services):
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"   # macOS/brew
mvn spring-boot:run
```

Or the prod-like stack (needs Docker):

```bash
docker compose --profile relational up --build   # PostgreSQL
# or
docker compose --profile document   up --build   # MongoDB
```

Open <http://localhost:8080/>.

## Walkthrough

1. **Home → Catalog.** Browse categories → products → items. *Talking point:* a read-only vertical
   slice migrated first (Phase 2) to prove the domain→persistence→web→test pipeline at lowest risk.

2. **Switch language** with the `EN / 日本語 / 中文` bar. Names, descriptions, and **prices** change.
   *Talking point:* the legacy per-locale `*_details` model is preserved faithfully; parity tests
   pin it (Phase 1).

3. **Register**, then **log in**. *Talking point:* the legacy `SignOnFilter` + CMP `User` became
   Spring Security form login with BCrypt — security intentionally *upgraded*, not reproduced
   bug-for-bug (ADR-0005, Phase 3).

4. **Add items to the cart**, adjust quantities. *Talking point:* a session cart that resolves live,
   localized prices through the catalog port (Phase 3).

5. **Check out.** Fill shipping, place the order, land on the confirmation. *Talking point:* the
   legacy fire-and-forget JMS hand-off to the Order Processing Center is preserved as an explicit
   `OrderSubmissionPort` seam; the default adapter records the hand-off, no broker required
   (ADR-0006, Phase 4). Watch the app log for the `Submitting order … to OPC seam` line.

6. **Show `/orders`** — durable order history the legacy storefront never kept (an intentional
   enhancement, risk R8).

7. **Re-run on the other datastore.** Restart with the `mongo` profile (or the `document` Compose
   profile) and repeat 1–6. *Talking point:* identical behavior on MongoDB — only the outbound
   persistence adapters changed, behind unchanged ports (ADR-0003/0004, Phase 5). `MongoPortParityTest`
   proves the two adapters honor the same contract.

## One-shot automated version

```bash
./scripts/demo.sh                                 # against a running instance (any datastore)
BASE_URL=http://localhost:8080 ./scripts/demo.sh
```
