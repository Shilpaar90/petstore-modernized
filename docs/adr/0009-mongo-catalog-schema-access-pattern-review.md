# ADR-0009: Reshape the MongoDB catalog schema around access patterns, not table-per-collection

## Status
Accepted (amends the document-model consequences of [ADR-0004](0004-relational-first-then-mongodb.md);
the relational-first sequencing decision in 0004 is unchanged)

## Context
[ADR-0004](0004-relational-first-then-mongodb.md) called for a document model that "embed[s]
`*_details` as localized sub-documents [and] embed[s] `items` in `product`." The implementation
that shipped in Phase 5 did neither: `catalog_categories`, `catalog_products`, and `catalog_items`
are three flat, top-level collections, one document per `(natural id, locale)`, keyed by a
synthetic `"{id}|{locale}"` string, with the parent id denormalized onto the child for read
access. This works, but the `_id` is a partitioning workaround rather than the entity's real
identity, and it was never validated against the app's actual access patterns — it was carried
over from the relational model's shape more than designed for the document model.

A workload-based review (list the access patterns, then decide embed vs. reference per
relationship from cardinality + how the data is actually read and written) was done against the
app's real query patterns:

1. List categories (~5, rarely changes).
2. List products in a category (~3–16 per category).
3. View one product **directly by URL** (`GET /products/{productId}`, `CatalogViewController`),
   which loads the product and all its item/SKU variants together in one request — no pattern
   ever queries an item independent of its parent product.
4. Add to cart from a product page (`itemId` already known; items are never looked up independent
   of their parent product).
5. Checkout → snapshot the cart into an order (write-once).
6. View one order / list a user's order history (owner-only).
7. Login (lookup by username).

| Relationship | Cardinality | Accessed together? | Growth | Decision |
|---|---|---|---|---|
| Category → Product | 1:few-to-many (~5 → ~9 avg) | No — products are hit directly by URL (pattern 3), not only via category | Bounded, admin-controlled | **Reference** (`catid` on product) |
| Product → Item | 1:few (2–6 SKUs) | Yes — always rendered together on one product page; no pattern queries an item independent of its product | Bounded, admin-controlled | **Embed** items inside product |
| Locale variants (all catalog entities) | 1:few (fixed at 3) | One locale read per request; no pattern ever lists all locales in the hot path | Bounded — locale count is app config, not user data | **Embed as a keyed map** (`i18n.en_US`, …), not partitioned as separate top-level documents |
| Order → OrderLine | 1:few (cart size) | Always read as a whole; never queried across orders by item | Bounded at checkout, immutable after | **Embed with snapshotted price/description** (already correct — unchanged by this ADR) |
| User → Order | 1:many, unbounded over a user's lifetime | Queried by owner; never "list a user with all orders inline" | Unbounded | **Reference** (`username` on order, already correct — unchanged) |
| Order → shipping info | 1:1 at time of purchase | N/A | N/A | **Embed as a snapshot**, not a live reference to the user profile (already correct — unchanged) |

The unifying principle: **anything transactional (an order) snapshots the mutable reference data
it touched at write time — line price, shipping address — so it can't silently change later.
Anything that's a stable, independently-queried entity (product, category) stays a real
reference.** Order and identity already followed this; catalog did not.

## Decision
Reshape the catalog document model:

- **`catalog_products`**: one document per `productid` (real natural key as `_id`, no locale
  suffix), embedding `catid` as a reference to `catalog_categories`, an `items: [...]` array
  (the former `catalog_items` collection folded in), and an `i18n` map keyed by locale
  (`i18n.en_US.name`, `i18n.ja_JP.descn`, …) replacing the document-per-locale partition.
- **`catalog_categories`**: unchanged shape (still a small, separate reference collection), but
  also moves locale from a partition key to an embedded `i18n` map for the same write-amplification
  reason — editing one English name currently means finding and updating one of N per-locale
  documents; with the map, it's a single field update on the one canonical document.
- **`catalog_items`** is retired as a top-level collection; its documents become the `items[]`
  array on the owning product.

**Indexes** — none exist today on any Mongo collection in this app (confirmed by grep: no
`@Indexed`/`@CompoundIndex` anywhere under `*/adapter/out/persistence/mongo`). Add:
- `catalog_products.catid` — every category-browse query filters on it and it isn't `_id`.
- `orders.username` — every order-history-by-owner query filters on it and it isn't `_id`.

**Java mapping** — the embedded locale map should be represented as `Map<String, LocalizedDetails>`
on the Mongo document classes (e.g. `ProductDocument.i18n : Map<String, LocalizedDetails>`, with
`LocalizedDetails` holding `name`/`image`/`descn`), rather than parsing ad hoc nested structures —
keeps the repository mapper a straight `Map.get(locale.toString())` instead of a query-time filter.

## Consequences
- One canonical document per product/category instead of one per `(id, locale)` — N-times fewer
  documents, and a single-document write for any content edit instead of a fan-out across locales.
- The synthetic `"{id}|{locale}"` `_id` goes away; `_id` becomes the entity's real natural key.
- `CatalogRepositoryMongoAdapter` and the `ItemDocumentRepository`/`ProductDocumentRepository`
  split collapse: item lookups become an in-memory filter over the parent product's `items[]`
  rather than a second collection query.
- The seed importer (`tools/seed-import/extract_catalog_seed.py --format mongo`, see the
  `seed-import` skill) needs a new emitter shape: one JSON document per product (with nested
  `items[]` and `i18n{}`) instead of the current flat per-(id, locale) row list.
- `MongoPortParityTest` and `MongoAppFlowIntegrationTest` need updating for the new document shape.
  This ADR records the target design; implementation had not yet landed as of authoring.
