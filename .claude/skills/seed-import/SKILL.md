---
name: seed-import
description: Regenerate the catalog seed data (Flyway V2 SQL and the Mongo JSON seed) from the legacy Java Pet Store `Populate-UTF8.xml`, via tools/seed-import/extract_catalog_seed.py. Use when the legacy source XML changes, a seed emitter needs a fix, or the relational/Mongo seeds need to be re-synced from one source of truth.
---

# Regenerate the catalog seed

`tools/seed-import/extract_catalog_seed.py` is the single source of truth for catalog seed data.
It parses the legacy `Populate-UTF8.xml` once and has two emitters, so the relational and document
seeds can never drift from each other. Stdlib-only Python 3 — no deps to install.

## Regenerate

From the repo root:
```bash
# relational (Flyway V2) — default format
python3 tools/seed-import/extract_catalog_seed.py \
  > src/main/resources/db/migration/V2__catalog_seed.sql

# document (MongoDB adapter seed)
python3 tools/seed-import/extract_catalog_seed.py --format mongo \
  > src/main/resources/db/mongo/catalog-seed.json
```

Each run prints row counts to stderr, e.g.:
```
[sql] 5 categories, 15 category_details, 16 products, 48 product_details, 28 items, 83 item_details
```
Run both and confirm the counts match between `sql` and `mongo` — a mismatch means the two
emitters have drifted and is a bug in the generator, not the data.

A different source XML can be passed as a positional arg (defaults to the in-repo provenance copy
at `tools/seed-import/legacy/Populate-UTF8.xml`):
```bash
python3 tools/seed-import/extract_catalog_seed.py --format sql /path/to/other/Populate-UTF8.xml
```

## Do not hand-edit the generated files

Both `V2__catalog_seed.sql` and `db/mongo/catalog-seed.json` are marked GENERATED. If the data
needs to change, fix the legacy XML or the emitter logic in `extract_catalog_seed.py`, then
re-run — never patch the generated output directly.

## Verify after regenerating

```bash
export JAVA_HOME="/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
./mvnw test -Dtest=CatalogRepositoryJpaAdapterCharacterizationTest   # relational parity
./mvnw test -Dtest=MongoPortParityTest                               # Mongo parity
```
Then run the full suite (see the `verify` skill) before committing — Flyway checksums the V2
migration, so changing it on a machine with an existing H2/Postgres schema will need a clean
database (fresh `./mvnw test` run, or drop the local dev DB).

## Gotchas
- The XML declares a DOCTYPE with external parameter entities (`dtds/*.dtd`); the script strips
  the DOCTYPE and does not validate against it — this is intentional, not a workaround to fix.
- `xml:lang="en-US"` (hyphen) maps to locale `"en_US"` (underscore) to match the legacy
  `Profile`/`PreferredLanguage` convention already used elsewhere in the schema.
- Item attributes are capped at 5 (`attr1`..`attr5` in SQL, an `attributes` array in Mongo) —
  this mirrors the legacy schema's fixed column count.
