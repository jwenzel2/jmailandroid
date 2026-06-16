# jmail Android

Native Jetpack Compose client for a compatible jmail server.

The debug build defaults to the jwenzel.net jmail server:

```text
https://mail.jwenzel.net
```

Users can still change the server URL on the first screen.

## Local build

Install JDK 17+, Android SDK platform 36, and Gradle 9.3+, then run:

```bash
gradle :app:assembleDebug
```

Configure Firebase by adding the deployment's generated `google-services.json` and applying the
Google Services Gradle plugin. The backend must expose `/api/v1` and the OIDC client must allow
`<PUBLIC_URL>/api/v1/mobile/callback` as a redirect URI.

## jwenzel.net Keycloak settings

For the `jmail` client in Keycloak at `https://auth.jwenzel.net`, allow these redirect URIs:

```text
https://mail.jwenzel.net/auth/callback
https://mail.jwenzel.net/api/v1/mobile/callback
```

Set the API environment from `.env.jwenzel.example`, replacing secrets and confirming the realm path:

```dotenv
PUBLIC_URL=https://mail.jwenzel.net
OIDC_ISSUER_URL=https://auth.jwenzel.net/realms/jmail
OIDC_REDIRECT_URI=https://mail.jwenzel.net/auth/callback
IMAP_HOST=mail.jwenzel.net
SMTP_HOST=mail.jwenzel.net
```
