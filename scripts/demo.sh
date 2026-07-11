#!/usr/bin/env bash
#
# End-to-end demo of the modernized Pet Store storefront: browse (with i18n), register, log in,
# add to cart, and place an order through the OPC seam. Drives the HTTP API the same way a user
# would, handling the CSRF token and session cookie.
#
# Works against ANY running instance regardless of datastore (H2 / Postgres / Mongo):
#
#   mvn spring-boot:run                                   # then, in another shell:
#   ./scripts/demo.sh
#   BASE_URL=http://localhost:8080 ./scripts/demo.sh      # override target
#
set -euo pipefail

BASE="${BASE_URL:-http://localhost:8080}"
JAR="$(mktemp)"                       # cookie jar
USER="demo-$$"                        # unique per run
PASS="petstore123"
trap 'rm -f "$JAR"' EXIT

say() { printf '\n\033[1;34m== %s\033[0m\n' "$1"; }

# Pull the hidden _csrf token out of a form page (fetched with the session cookie).
csrf() { curl -s -b "$JAR" "$1" | grep -oE 'name="_csrf" value="[^"]+"' | head -1 | sed -E 's/.*value="([^"]+)".*/\1/'; }

say "Health"
curl -s "$BASE/actuator/health" && echo

say "Browse catalog (JSON API, default locale en_US)"
curl -s "$BASE/api/catalog/categories"; echo

say "i18n: the FISH category in Japanese (?lang=ja_JP)"
curl -s "$BASE/api/catalog/categories/FISH?lang=ja_JP"; echo

say "Register user '$USER'"
curl -s -c "$JAR" "$BASE/register" >/dev/null
curl -s -b "$JAR" -c "$JAR" -o /dev/null -w '  register -> HTTP %{http_code} %{redirect_url}\n' \
  --data-urlencode "username=$USER" --data-urlencode "password=$PASS" --data-urlencode "_csrf=$(csrf "$BASE/register")" \
  "$BASE/register"

say "Log in"
curl -s -b "$JAR" -c "$JAR" -o /dev/null -w '  login -> HTTP %{http_code} %{redirect_url}\n' \
  --data-urlencode "username=$USER" --data-urlencode "password=$PASS" --data-urlencode "_csrf=$(csrf "$BASE/login")" \
  "$BASE/login"

say "Add 2x EST-1 (Angelfish) to the cart"
curl -s -b "$JAR" -c "$JAR" -o /dev/null -w '  add -> HTTP %{http_code} %{redirect_url}\n' \
  --data-urlencode "itemId=EST-1" --data-urlencode "quantity=2" --data-urlencode "_csrf=$(csrf "$BASE/items/EST-1")" \
  "$BASE/cart/add"

say "Check out"
LOC="$(curl -s -b "$JAR" -c "$JAR" -o /dev/null -w '%{redirect_url}' \
  --data-urlencode "name=Demo User" --data-urlencode "addressLine=1 Main St" \
  --data-urlencode "city=Springfield" --data-urlencode "email=demo@example.com" \
  --data-urlencode "_csrf=$(csrf "$BASE/checkout")" "$BASE/checkout")"
echo "  order confirmation at: $LOC"

say "Order confirmation"
PAGE="$(curl -s -b "$JAR" "$LOC")"
echo "$PAGE" | grep -qE 'your order is placed' && echo "  ✅ order placed"
echo "  order id: ${LOC##*/}"
echo "  status:   $(echo "$PAGE" | grep -oE 'status <span[^>]*>[A-Z]+' | grep -oE '[A-Z]+$')"
echo "  total:    $(echo "$PAGE" | grep -oE '<td class="price">[0-9]+\.[0-9]{2}</td>' | tail -1 | grep -oE '[0-9]+\.[0-9]{2}')"

say "Done — cart is now empty"
curl -s -b "$JAR" "$BASE/cart" | grep -oE 'Your cart is empty' | head -1
