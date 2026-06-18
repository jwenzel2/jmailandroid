# jmailandroid

Native Android client for a compatible jmail server.

The app is built with Kotlin and Jetpack Compose. It connects to the jmail mobile API for Keycloak
login, mail, contacts, calendar, and push device registration.

## Current Features

- Keycloak/OIDC login through the jmail mobile callback flow.
- Persistent signed-in session backed by local Android preferences with mobile token expiry tracking
  and proactive renewal.
- Mail account add/edit/remove flow, folders, account/folder drawer, message list, message detail,
  search, pagination, quick filters, read/unread, star/unstar, spam, delete, move, and bulk actions.
- Compose, reply, and forward flows with basic client-side validation.
- Attachment download/share through Android `FileProvider`.
- Contacts list, search, detail view, create/edit, and multi-select delete.
- Calendar agenda, week, and month views with create/edit/delete event support.
- Dark theme setting and blue Material color scheme.
- Firebase Cloud Messaging registration hooks for mail push notifications.

## Repository Layout

```text
apps/android/     Android Studio project
migrations/       Mobile API database migration copied from the jmail server work
.env*.example     Server-side environment examples for the compatible jmail API
```

## Build

Open `apps/android` in Android Studio, or build from the command line:

```bash
cd apps/android
./gradlew :app:assembleDebug
```

This workspace has also been verified with the Android Studio bundled JBR and local Gradle cache:

```bash
cd apps/android
JAVA_HOME=/opt/android-studio/jbr ANDROID_HOME=$HOME/Android/Sdk ./gradlew :app:assembleDebug
```

## Release Signing

Release signing is configured but intentionally does not store secrets in the repository. Provide
signing values either as environment variables:

```bash
export JMAILANDROID_KEYSTORE_FILE=/absolute/path/to/jmailandroid-release.jks
export JMAILANDROID_KEYSTORE_PASSWORD=...
export JMAILANDROID_KEY_ALIAS=...
export JMAILANDROID_KEY_PASSWORD=...
cd apps/android
./gradlew :app:assembleRelease
```

Or create an untracked `apps/android/keystore.properties` file:

```properties
storeFile=/absolute/path/to/jmailandroid-release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

## Release Prep

- Privacy notes template: [`docs/PRIVACY.md`](docs/PRIVACY.md)
- Release notes template: [`docs/RELEASE_NOTES.md`](docs/RELEASE_NOTES.md)
- Real-device QA checklist: [`docs/QA_CHECKLIST.md`](docs/QA_CHECKLIST.md)

## Server Requirements

The Android app expects a deployed jmail server that exposes:

- `GET /api/v1/compatibility`
- `GET /api/v1/mobile/login`
- `GET /api/v1/mobile/callback`
- `POST /api/v1/mobile/exchange`
- `POST /api/v1/mobile/token`
- `PUT /api/v1/mobile/devices`
- `DELETE /api/v1/mobile/devices/:installationId`
- mail, contact, and calendar API routes used by the original jmail web app

For Keycloak, the OIDC client must allow:

```text
https://mail.jwenzel.net/api/v1/mobile/callback
```

The debug build defaults to:

```text
https://mail.jwenzel.net
```

Users can still enter a different compatible server URL on the first screen.

## Push Notifications

The app contains Firebase Messaging integration, but production push requires deployment-specific
Firebase configuration:

- add the generated `google-services.json`
- apply/configure the Google Services Gradle plugin
- configure the server to send FCM notifications to registered mobile devices

## Release Work Remaining

- Deploy the mobile token renewal API and verify long-lived login against production.
- Verify server-side account provisioning against the production mobile API.
- Finalize store privacy disclosures for the actual deployment.
- Complete and record a full real-device QA pass against `mail.jwenzel.net`.
