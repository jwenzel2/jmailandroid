#!/usr/bin/env sh
set -eu

# Configure a Keycloak realm's WebAuthn policy so roaming security keys
# (USB + NFC) are accepted for login, in addition to platform passkeys.
#
# The lever is the "authenticator attachment": leaving it unset (the default
# below) allows BOTH platform authenticators (on-device fingerprint/face) and
# cross-platform / roaming authenticators (USB, NFC, BLE security keys). Set it
# to "cross-platform" instead if you want to require a roaming security key.
#
# Transports (usb/nfc/ble) are negotiated by the browser and authenticator at
# ceremony time; there is no per-transport Keycloak field to toggle.
#
# Usage:
#   KC_SERVER=https://auth.jwenzel.net \
#   KC_REALM=jmail \
#   KC_RP_ID=auth.jwenzel.net \
#   KC_ADMIN=admin KC_ADMIN_PASSWORD=secret \
#   [ATTACHMENT="not specified"] \
#   sh scripts/keycloak-webauthn-security-keys.sh
#
# Requires kcadm.sh on PATH (ships with the Keycloak distribution under bin/).

KC_SERVER="${KC_SERVER:-http://localhost:8080}"
KC_REALM="${KC_REALM:-jmail}"
KC_RP_ID="${KC_RP_ID:-}"
KC_RP_NAME="${KC_RP_NAME:-jmail}"
KC_ADMIN="${KC_ADMIN:-admin}"
KC_ADMIN_PASSWORD="${KC_ADMIN_PASSWORD:-}"
KC_ADMIN_REALM="${KC_ADMIN_REALM:-master}"
# "not specified" = platform + roaming security keys; "cross-platform" = keys only.
ATTACHMENT="${ATTACHMENT:-not specified}"

[ -n "$KC_ADMIN_PASSWORD" ] || { echo "FAIL: set KC_ADMIN_PASSWORD" >&2; exit 1; }
[ -n "$KC_RP_ID" ] || { echo "FAIL: set KC_RP_ID (e.g. auth.jwenzel.net)" >&2; exit 1; }
command -v kcadm.sh >/dev/null 2>&1 || { echo "FAIL: kcadm.sh not on PATH" >&2; exit 1; }

kcadm.sh config credentials \
  --server "$KC_SERVER" \
  --realm "$KC_ADMIN_REALM" \
  --user "$KC_ADMIN" \
  --password "$KC_ADMIN_PASSWORD"

# Passwordless policy drives passkey / security-key login. The same field names
# without the "Passwordless" segment configure the WebAuthn second-factor flow.
kcadm.sh update "realms/$KC_REALM" -f - <<JSON
{
  "webAuthnPolicyPasswordlessRpEntityName": "$KC_RP_NAME",
  "webAuthnPolicyPasswordlessRpId": "$KC_RP_ID",
  "webAuthnPolicyPasswordlessSignatureAlgorithms": ["ES256", "RS256"],
  "webAuthnPolicyPasswordlessAuthenticatorAttachment": "$ATTACHMENT",
  "webAuthnPolicyPasswordlessRequireResidentKey": "not specified",
  "webAuthnPolicyPasswordlessUserVerificationRequirement": "preferred",
  "webAuthnPolicyPasswordlessAttestationConveyancePreference": "not specified",
  "webAuthnPolicyPasswordlessCreateTimeout": 0,
  "webAuthnPolicyPasswordlessAvoidSameAuthenticatorRegister": false,
  "webAuthnPolicyPasswordlessAcceptableAaguids": []
}
JSON

echo "OK: WebAuthn passwordless policy updated for realm '$KC_REALM' (attachment: $ATTACHMENT, rpId: $KC_RP_ID)"
echo "Next: bind the 'WebAuthn Passwordless' authenticator into the realm's browser flow."
