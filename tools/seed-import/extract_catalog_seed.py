#!/usr/bin/env python3
"""
Generate the catalog seed from the legacy Pet Store populate XML — in two shapes from one source.

This is a one-way migration tool: it reads the original `Populate-UTF8.xml` (from the Java Pet
Store 1.3.1 distribution) and emits the catalog seed in either:

  * ``sql``   — idempotent-ordered INSERTs matching the V1 relational schema (Flyway V2), OR
  * ``mongo`` — a nested JSON document seed matching the target Mongo document shape (ADR-0009):
    one document per category/product, items embedded in their product, locale folded into an
    ``i18n`` map instead of a document-per-locale partition.

Keeping ONE generator with two emitters preserves a single source of truth: the relational and
document seeds can never drift, because both are produced from the same legacy XML.

Usage:
    python3 extract_catalog_seed.py [--format sql|mongo] [SOURCE_XML]
        > ../../src/main/resources/db/migration/V2__catalog_seed.sql        # sql (default)
        > ../../src/main/resources/db/mongo/catalog-seed.json               # mongo

SOURCE_XML defaults to the in-repo provenance copy at ./legacy/Populate-UTF8.xml.

Mappings:
  * xml:lang "en-US" -> locale "en_US" (legacy stored underscore form; cf. Profile/PreferredLanguage)
  * <Attribute> elements -> attr1..attr5 (SQL) / an "attributes" array (Mongo)
  * Categories have Name+Image; Products/Items add Description; Items add ListPrice/UnitCost
  * Mongo: catid is denormalized onto product docs (still a reference, not embedded — see
    ADR-0009); items are embedded arrays on their product, not a standalone collection.
  * image is locale-invariant in the legacy data, so Mongo hoists it out of ``i18n`` to the
    document/item's top level, keyed by the first detail row seen for that id.
"""
from __future__ import annotations

import json
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

DEFAULT_SRC = Path(__file__).parent / "legacy" / "Populate-UTF8.xml"
MAX_ATTRS = 5


def load_catalog(path: Path) -> ET.Element:
    raw = path.read_text(encoding="utf-8")
    # The document declares an internal DTD subset that pulls in external parameter entities
    # (dtds/*.dtd). We do not validate; strip the DOCTYPE so a plain parser accepts the body.
    raw = re.sub(r"<!DOCTYPE.*?\]>", "", raw, flags=re.DOTALL)
    return ET.fromstring(raw)


def q(value: str | None) -> str:
    """SQL string literal with single-quote escaping; NULL for missing."""
    if value is None:
        return "NULL"
    return "'" + value.strip().replace("'", "''") + "'"


def num(value: str) -> str:
    return value.strip()


def locale_of(el: ET.Element) -> str:
    lang = el.get("{http://www.w3.org/XML/1998/namespace}lang") or el.get("lang") or "en-US"
    return lang.replace("-", "_")


def text(el: ET.Element, tag: str) -> str | None:
    child = el.find(tag)
    return child.text if child is not None and child.text is not None else None


def strip(value: str | None) -> str | None:
    return value.strip() if value is not None else None


def parse(root: ET.Element) -> dict:
    """Parse the legacy XML into plain rows, shared by both emitters."""
    catalog = root.find("Catalog")
    model: dict = {"categories": [], "category_details": [], "products": [],
                   "product_details": [], "items": [], "item_details": []}

    for cat in catalog.find("Categories").findall("Category"):
        cid = cat.get("id")
        model["categories"].append({"catid": cid})
        for d in cat.findall("CategoryDetails"):
            model["category_details"].append({
                "catid": cid, "locale": locale_of(d), "name": strip(text(d, "Name")),
                "image": strip(text(d, "Image")), "descn": strip(text(d, "Description")),
            })

    for p in catalog.find("Products").findall("Product"):
        pid, cid = p.get("id"), p.get("category")
        model["products"].append({"productid": pid, "catid": cid})
        for d in p.findall("ProductDetails"):
            model["product_details"].append({
                "productid": pid, "catid": cid, "locale": locale_of(d), "name": strip(text(d, "Name")),
                "image": strip(text(d, "Image")), "descn": strip(text(d, "Description")),
            })

    for it in catalog.find("Items").findall("Item"):
        iid, pid = it.get("id"), it.get("product")
        model["items"].append({"itemid": iid, "productid": pid})
        for d in it.findall("ItemDetails"):
            attrs = [strip(a.text) for a in d.findall("Attribute") if a.text and a.text.strip()][:MAX_ATTRS]
            model["item_details"].append({
                "itemid": iid, "productid": pid, "locale": locale_of(d),
                "listprice": num(text(d, "ListPrice")), "unitcost": num(text(d, "UnitCost")),
                "image": strip(text(d, "Image")), "descn": strip(text(d, "Description")),
                "attributes": attrs,
            })

    return model


def emit_sql(model: dict, src_name: str, out) -> None:
    out.write("-- V2 — Catalog seed data\n")
    out.write("-- GENERATED by tools/seed-import/extract_catalog_seed.py from the legacy\n")
    out.write(f"-- {src_name}. Do not edit by hand; re-run the generator instead.\n\n")

    cats = [f"insert into category (catid) values ({q(c['catid'])});" for c in model["categories"]]
    cat_details = [
        "insert into category_details (catid, name, image, descn, locale) values "
        f"({q(d['catid'])}, {q(d['name'])}, {q(d['image'])}, {q(d['descn'])}, {q(d['locale'])});"
        for d in model["category_details"]
    ]
    prods = [f"insert into product (productid, catid) values ({q(p['productid'])}, {q(p['catid'])});"
             for p in model["products"]]
    prod_details = [
        "insert into product_details (productid, locale, name, image, descn) values "
        f"({q(d['productid'])}, {q(d['locale'])}, {q(d['name'])}, {q(d['image'])}, {q(d['descn'])});"
        for d in model["product_details"]
    ]
    items = [f"insert into item (itemid, productid) values ({q(i['itemid'])}, {q(i['productid'])});"
             for i in model["items"]]
    item_details = []
    for d in model["item_details"]:
        attrs = list(d["attributes"]) + [None] * (MAX_ATTRS - len(d["attributes"]))
        attr_sql = ", ".join(q(a) for a in attrs)
        item_details.append(
            "insert into item_details (itemid, listprice, unitcost, locale, image, descn, "
            "attr1, attr2, attr3, attr4, attr5) values "
            f"({q(d['itemid'])}, {d['listprice']}, {d['unitcost']}, {q(d['locale'])}, "
            f"{q(d['image'])}, {q(d['descn'])}, {attr_sql});"
        )

    for title, rows in [
        ("categories", cats), ("category_details", cat_details),
        ("products", prods), ("product_details", prod_details),
        ("items", items), ("item_details", item_details),
    ]:
        out.write(f"-- {title} ({len(rows)})\n")
        out.write("\n".join(rows))
        out.write("\n\n")


def emit_mongo(model: dict, src_name: str, out) -> None:
    """Nested document seed matching the target Mongo shape (ADR-0009): one document per
    category/product keyed by its real natural id, locale-varying fields folded into an ``i18n``
    map, and items embedded inside their owning product — not a flat per-(id, locale) row list."""

    def group_i18n(details: list[dict], key: str, locale_fields: dict) -> tuple[dict, dict]:
        """Groups detail rows by their parent id into an i18n map, plus the (locale-invariant)
        image keyed by the same id, taken from the first row seen for that id."""
        i18n: dict = {}
        image: dict = {}
        for d in details:
            i18n.setdefault(d[key], {})[d["locale"]] = {f: d[f] for f in locale_fields}
            image.setdefault(d[key], d.get("image"))
        return i18n, image

    cat_i18n, cat_image = group_i18n(model["category_details"], "catid", {"name": None, "descn": None})
    categories_out = [
        {"catid": c["catid"], "image": cat_image.get(c["catid"]), "i18n": cat_i18n.get(c["catid"], {})}
        for c in model["categories"]
    ]

    prod_i18n, prod_image = group_i18n(model["product_details"], "productid", {"name": None, "descn": None})

    item_i18n: dict = {}
    item_image: dict = {}
    for d in model["item_details"]:
        item_i18n.setdefault(d["itemid"], {})[d["locale"]] = {
            "listprice": d["listprice"], "unitcost": d["unitcost"],
            "descn": d["descn"], "attributes": list(d["attributes"]),
        }
        item_image.setdefault(d["itemid"], d.get("image"))

    items_by_product: dict = {}
    for it in model["items"]:
        items_by_product.setdefault(it["productid"], []).append(it["itemid"])

    products_out = []
    for p in model["products"]:
        pid = p["productid"]
        items_out = [
            {"itemid": iid, "image": item_image.get(iid), "i18n": item_i18n.get(iid, {})}
            for iid in items_by_product.get(pid, [])
        ]
        products_out.append({
            "productid": pid, "catid": p["catid"], "image": prod_image.get(pid),
            "i18n": prod_i18n.get(pid, {}), "items": items_out,
        })

    doc = {"_generatedFrom": src_name, "categories": categories_out, "products": products_out}
    json.dump(doc, out, ensure_ascii=False, indent=2)
    out.write("\n")


def main() -> None:
    args = sys.argv[1:]
    fmt = "sql"
    if args and args[0] == "--format":
        fmt = args[1]
        args = args[2:]
    src = Path(args[0]) if args else DEFAULT_SRC

    model = parse(load_catalog(src))

    if fmt == "sql":
        emit_sql(model, src.name, sys.stdout)
    elif fmt == "mongo":
        emit_mongo(model, src.name, sys.stdout)
    else:
        sys.exit(f"unknown --format: {fmt} (expected sql|mongo)")

    sys.stderr.write(
        f"[{fmt}] {len(model['categories'])} categories, {len(model['category_details'])} category_details, "
        f"{len(model['products'])} products, {len(model['product_details'])} product_details, "
        f"{len(model['items'])} items, {len(model['item_details'])} item_details\n"
    )


if __name__ == "__main__":
    main()
