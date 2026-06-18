# jmailandroid Real-Device QA Checklist

Use this checklist for a release candidate against `https://mail.jwenzel.net` or another compatible
jmail deployment.

## Test Device

- Device model:
- Android version:
- App version:
- Build type: debug / release
- Server URL:
- Tester:
- Date:

## Install And Login

- Install the APK cleanly.
- Launch the app and confirm the default server is `https://mail.jwenzel.net`.
- Complete Keycloak login.
- Confirm the app returns from browser login to the native app.
- Close and reopen the app; confirm the session remains signed in.
- Sign out from Settings; confirm the app returns to onboarding.
- Sign in again after sign out.

## Mail

- Open the mail drawer with the hamburger button.
- Select a folder and confirm the drawer closes.
- Switch between available accounts if more than one account exists.
- Open a message from the message list.
- Confirm unread/read visual state updates.
- Mark a message read and unread.
- Star and unstar a message.
- Use All, Unread, and Starred filters.
- Search within the current folder.
- Load the next page of messages when available.
- Reply to a message.
- Forward a message.
- Compose and send a new message.
- Confirm compose validation catches missing recipients.
- Confirm attachment indicator appears for a message with attachments.
- Download/share an attachment.
- Move a message to another folder.
- Mark a message as spam.
- Select multiple messages and run bulk read/unread/star/unstar/delete/move actions.

## Mail Accounts

- Add a Keycloak-backed IMAP account.
- Add or validate a password/app-password account if the deployment supports it.
- Open account details from the drawer.
- Edit account settings and confirm the updated values persist after refresh.
- Remove a test account and confirm it disappears from the account list.

## Contacts

- Open Contacts.
- Search contacts.
- Open a contact detail card.
- Confirm empty contact fields are hidden rather than shown as `null`.
- Create a contact.
- Edit the contact.
- Select one or more contacts.
- Delete selected test contacts.

## Calendar

- Open Calendar.
- Switch between Agenda, Week, and Month.
- Confirm Agenda is today-only.
- Navigate Week and Month views with previous/today/next controls.
- Create an event from each relevant view.
- Edit an event.
- Delete a test event.
- Confirm server-side changes appear after refresh/reopen.

## Settings And Theme

- Toggle dark theme.
- Close and reopen the app; confirm theme preference persists.
- Toggle push notifications off and on.
- Use re-register notifications.
- Confirm server/profile information displays without errors.

## Push Notifications

- Grant notification permission on Android 13+.
- Trigger a new-mail push from the server.
- Confirm notification title/body are sensible.
- Tap behavior, if configured, should not crash the app.
- Disable push notifications and confirm no further notifications are shown.

## Session Renewal

- Confirm the server exposes `POST /api/v1/mobile/token`.
- Confirm app remains signed in after restart.
- If possible, test with a shortened mobile token TTL in a staging deployment.
- Confirm proactive renewal replaces the stored mobile token before expiry.
- Confirm expired tokens return the app to sign-in with a clear message.

## Release Build

- Build release APK/AAB with signing values configured.
- Install the release build on a real device.
- Repeat the critical login, mail open, send, contacts, calendar, settings, and notification tests.

## Results

- Pass / Fail:
- Blocking issues:
- Non-blocking issues:
- Notes:
