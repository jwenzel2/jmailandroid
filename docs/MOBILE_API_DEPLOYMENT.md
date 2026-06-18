# Mobile API Deployment Handoff

This repository includes the Android client plus a copied mobile API snapshot for the upstream jmail
server. The API files under `apps/api/src` are handoff source, not a standalone buildable server
inside this Android repository.

## Files To Merge Into The jmail Server

- `apps/api/src/routes/mobile.ts`
- `apps/api/src/repositories/mobileAuth.ts`
- `apps/api/src/repositories/mobileDevices.ts`
- `apps/api/src/repositories/mobileTokens.ts`
- `apps/api/src/repositories/mailAccounts.ts`
- `apps/api/src/plugins/session.ts` changes, if the upstream server does not already support bearer
  mobile token auth
- `apps/api/src/server.ts` route registration, if the upstream server has not already registered
  `mobileRoutes`
- `packages/shared/src/mobile.ts`, if the upstream shared package is missing the mobile schemas
- `migrations/1700000003000_mobile_accounts.sql`

The copied API imports upstream jmail modules that are not present in this Android repo, including
`config`, `oidc`, `sessions`, `users`, and `guards`. Merge and test in the full jmail server
repository.

## Required Public Routes

The Android app expects these routes on the configured server:

- `GET /api/v1/compatibility`
- `GET /api/v1/mobile/login?redirect_uri=jmail%3A%2F%2Fauth`
- `GET /api/v1/mobile/callback`
- `POST /api/v1/mobile/exchange`
- `POST /api/v1/mobile/token`
- `DELETE /api/v1/mobile/token`
- `PUT /api/v1/mobile/devices`
- `DELETE /api/v1/mobile/devices/:installationId`
- `GET /api/v1/me`
- `GET /api/v1/accounts`
- `POST /api/v1/accounts`
- `PATCH /api/v1/accounts/:id`
- `DELETE /api/v1/accounts/:id`

The existing mail, contacts, and calendar API routes used by the web app also need to remain
available to bearer-authenticated mobile sessions.

## Keycloak/OIDC Requirements

For the `jwenzel.net` deployment, the Keycloak client must allow:

```text
https://mail.jwenzel.net/api/v1/mobile/callback
```

The Android app deep link remains:

```text
jmail://auth
```

The server starts the mobile login flow and uses the public HTTPS callback; the callback exchanges
with Keycloak, creates a short mobile auth code, and redirects back to `jmail://auth?code=...`.

## Nginx/Proxy Requirements

Proxy the versioned mobile API to the jmail API service:

```nginx
location /api/v1/ {
    proxy_pass http://127.0.0.1:4000;
    proxy_set_header Host $host;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
```

Keep the existing web app/static proxy behavior for all other paths.

## Smoke Tests

Run these against production after deploy:

```bash
curl -sk https://mail.jwenzel.net/api/v1/compatibility

curl -skI 'https://mail.jwenzel.net/api/v1/mobile/login?redirect_uri=jmail%3A%2F%2Fauth' \
  | grep -i '^location:'
```

Expected compatibility response includes:

```json
{
  "service": "jmail",
  "apiVersion": 1,
  "features": [
    "mail-accounts",
    "mail",
    "calendar",
    "contacts",
    "fcm-devices",
    "mobile-token-renewal"
  ]
}
```

After Android login, verify the returned mobile token can call:

```bash
curl -sk -H "Authorization: Bearer $TOKEN" https://mail.jwenzel.net/api/v1/me
curl -sk -H "Authorization: Bearer $TOKEN" https://mail.jwenzel.net/api/v1/accounts
curl -sk -X POST -H "Authorization: Bearer $TOKEN" https://mail.jwenzel.net/api/v1/mobile/token
```

## Database Migration

Apply `migrations/1700000003000_mobile_accounts.sql` in the full jmail server database migration
flow. It creates:

- `mobile_tokens`
- `mobile_auth_transactions`
- `mobile_auth_codes`
- `mail_accounts`
- `mobile_devices`

## Android Verification After Server Deploy

Use [`docs/QA_CHECKLIST.md`](QA_CHECKLIST.md), with special attention to:

- login redirect returning to Android
- token renewal before expiry
- account add/edit/remove
- push device registration
- mail, contacts, and calendar calls using bearer mobile auth
