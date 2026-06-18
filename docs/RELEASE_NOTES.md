# jmailandroid Release Notes

## 0.1.0

Initial Android client preview for compatible jmail servers.

### Included

- Keycloak/OIDC sign-in through the jmail mobile API.
- Persistent mobile session with token expiry tracking and proactive renewal.
- Mail folder drawer, message list, search, pagination, quick filters, message detail, attachments,
  compose, reply, forward, read/unread, star/unstar, spam, delete, move, and bulk actions.
- Mail account add, edit, detail, and remove flows.
- Contacts list, search, detail, create/edit, and multi-select delete.
- Calendar agenda, week, and month views with event create/edit/delete.
- Dark theme setting and blue Material color scheme.
- Firebase Cloud Messaging registration hooks for new-mail notifications.
- Release signing configuration through environment variables or local untracked properties.

### Deployment Notes

- Requires a compatible jmail server exposing the documented `/api/v1` mobile routes.
- The OIDC client must allow the mobile callback redirect URI.
- Push notifications require deployment-specific Firebase configuration.
- Real-device QA against the production server should be completed before public distribution.
