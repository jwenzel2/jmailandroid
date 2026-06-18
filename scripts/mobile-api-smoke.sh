#!/usr/bin/env sh
set -eu

SERVER_URL="${1:-${JMOBILE_SERVER_URL:-https://mail.jwenzel.net}}"
TOKEN="${2:-${JMOBILE_TOKEN:-}}"

SERVER_URL="${SERVER_URL%/}"

fail() {
  printf 'FAIL: %s\n' "$1" >&2
  exit 1
}

need_feature() {
  feature="$1"
  printf '%s' "$COMPATIBILITY" | grep -q "\"$feature\"" || fail "compatibility missing feature: $feature"
}

printf 'Checking jmail mobile API at %s\n' "$SERVER_URL"

COMPATIBILITY="$(curl -fsSk "$SERVER_URL/api/v1/compatibility")" ||
  fail 'GET /api/v1/compatibility failed'

printf '%s' "$COMPATIBILITY" | grep -q '"service"[[:space:]]*:[[:space:]]*"jmail"' ||
  fail 'compatibility service is not jmail'
printf '%s' "$COMPATIBILITY" | grep -q '"apiVersion"[[:space:]]*:[[:space:]]*1' ||
  fail 'compatibility apiVersion is not 1'

need_feature 'mail-accounts'
need_feature 'mail'
need_feature 'calendar'
need_feature 'contacts'
need_feature 'fcm-devices'
need_feature 'mobile-token-renewal'

LOGIN_HEADERS="$(
  curl -fsSkI "$SERVER_URL/api/v1/mobile/login?redirect_uri=jmail%3A%2F%2Fauth"
)" || fail 'GET /api/v1/mobile/login failed'

printf '%s' "$LOGIN_HEADERS" | grep -qi '^location: ' ||
  fail 'mobile login did not return a redirect location'
printf '%s' "$LOGIN_HEADERS" | grep -qi '/protocol/openid-connect/auth' ||
  fail 'mobile login redirect does not look like an OIDC auth URL'
printf '%s' "$LOGIN_HEADERS" | grep -qi 'redirect_uri=' ||
  fail 'mobile login redirect is missing redirect_uri'

if [ -n "$TOKEN" ]; then
  curl -fsSk -H "Authorization: Bearer $TOKEN" "$SERVER_URL/api/v1/me" >/dev/null ||
    fail 'bearer GET /api/v1/me failed'
  curl -fsSk -H "Authorization: Bearer $TOKEN" "$SERVER_URL/api/v1/accounts" >/dev/null ||
    fail 'bearer GET /api/v1/accounts failed'
  curl -fsSk -X POST -H "Authorization: Bearer $TOKEN" "$SERVER_URL/api/v1/mobile/token" >/dev/null ||
    fail 'bearer POST /api/v1/mobile/token failed'
else
  printf 'Skipping bearer checks; pass a mobile token as argument 2 or JMOBILE_TOKEN.\n'
fi

printf 'PASS: mobile API smoke checks completed.\n'
