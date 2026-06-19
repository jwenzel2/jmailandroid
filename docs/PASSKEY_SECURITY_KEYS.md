# Passkeys & security keys (USB + NFC) for jmail login

jmail's mobile login is an OIDC redirect to Keycloak, opened in a **Custom Tab**
(`MainActivity.launchLogin`). The Custom Tab runs a real browser (Chrome/Brave/
Firefox/Edge), so WebAuthn ceremonies reach the OS FIDO2 stack — which is what
lets a **USB** (OTG) or **NFC**-tapped security key respond. An in-app WebView
cannot do this, which is why login is never rendered in one.

Whether security keys are actually *offered* is decided by the **Keycloak realm
WebAuthn policy**, not the app. The two pieces:

## 1. Android app (this repo)

Already handled:

- Login opens in a Custom Tab pinned to a WebAuthn-capable browser
  (`WEBAUTHN_CAPABLE_BROWSERS`), falling back to the system browser. No WebView.
- `AndroidManifest.xml` declares `<queries>` for `https` VIEW intents and the
  Custom Tabs service so browser detection works under Android 11+ package
  visibility.

Nothing in the app needs USB or NFC permissions: while the Custom Tab is in the
foreground, the **browser** owns the security-key transport (USB host / NFC
dispatch), not jmail.

## 2. Keycloak realm policy (the actual lever)

The deciding field is **Authenticator Attachment** on the WebAuthn policy:

| Value             | What it allows                                              |
| ----------------- | ---------------------------------------------------------- |
| `not specified`   | Platform passkeys **and** roaming USB/NFC/BLE security keys |
| `cross-platform`  | Roaming security keys only (USB/NFC/BLE)                    |
| `platform`        | On-device only — **blocks** USB/NFC keys                   |

To support USB **and** NFC security keys, use `not specified` (both) or
`cross-platform` (keys only). There is **no per-transport setting** in Keycloak —
USB/NFC/BLE are negotiated by the browser and the key itself; allowing roaming
authenticators is the whole switch. Requirements:

- The realm/RP must be served over **HTTPS**, and the policy **RP ID** must match
  the login domain (e.g. `auth.jwenzel.net`).
- Bind **WebAuthn Passwordless** into the browser flow for passkey login, or
  **WebAuthn** for a second factor.

### Apply via script

```sh
KC_SERVER=https://auth.jwenzel.net \
KC_REALM=jmail \
KC_RP_ID=auth.jwenzel.net \
KC_ADMIN=admin KC_ADMIN_PASSWORD=… \
sh scripts/keycloak-webauthn-security-keys.sh
```

`scripts/keycloak-webauthn-security-keys.sh` sets the passwordless policy to
accept ES256/RS256, `userVerification = preferred`, and attachment
`not specified` (override with `ATTACHMENT="cross-platform"` for keys-only). It
configures the policy only; bind the authenticator to the flow afterward.

### Apply via Admin Console

1. **Realm settings → Security defenses → WebAuthn Passwordless Policy**
   (or **WebAuthn Policy** for 2FA).
2. **Relying Party ID** = your login host (e.g. `auth.jwenzel.net`).
3. **Authenticator Attachment** = `not specified` (or `cross-platform`).
4. **Signature Algorithms** = `ES256`, `RS256`.
5. **Authentication → Flows**: add **WebAuthn Passwordless** to the browser flow.

## Verifying

1. Run the script (or set the policy in the console).
2. Register a security key for a test user (Account console → Signing in).
3. On the phone, tap **Continue with Keycloak** — the Custom Tab opens.
4. Insert a USB-OTG key (or tap an NFC key) and complete the prompt; the tab
   redirects back to `jmail://auth` and the app exchanges the code.

If the browser never offers a security key, the policy attachment is almost
certainly still `platform`.
