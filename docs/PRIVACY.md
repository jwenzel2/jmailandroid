# jmailandroid Privacy Notes

This document is a release-prep template for the Android app. Review and adapt it before publishing
the app publicly or submitting it to an app store.

## Data The App Handles

jmailandroid connects to a compatible jmail server chosen by the user or deployment. The app may
display and transmit:

- account identity returned by the configured jmail server
- email message metadata and message bodies
- mail attachments opened or shared by the user
- contacts and address book entries
- calendar events
- mail account settings entered in the app
- Firebase Cloud Messaging device token, if push notifications are enabled

The app stores its mobile session token locally using Android preferences with Android Keystore
encryption for the token value.

## Data Sharing

The app communicates with:

- the configured jmail server for login, mail, contacts, calendar, and device registration
- the configured Keycloak/OIDC identity provider during sign-in
- Firebase Cloud Messaging when push notification support is configured

The app does not include advertising or analytics SDKs.

## User Controls

Users can:

- sign out from Settings
- disable push notifications from Settings
- remove configured mail accounts from the mail account detail screen
- change the configured server by signing out and entering a different compatible server

## Store Disclosure Checklist

Before release, confirm the final store privacy form reflects the deployed configuration:

- whether Firebase Cloud Messaging is enabled
- whether the deployed server logs request metadata
- whether account passwords or app passwords are permitted for added mail accounts
- the public contact address for privacy requests
- the public privacy policy URL, if publishing outside a private/internal channel
