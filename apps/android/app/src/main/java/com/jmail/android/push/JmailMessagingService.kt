package com.jmail.android.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jmail.android.JmailApplication
import com.jmail.android.data.JmailApi
import com.jmail.android.data.SessionStore

class JmailMessagingService : FirebaseMessagingService() {
    @Suppress("OVERRIDE_DEPRECATION")
    override fun onNewToken(token: String) {
        val session = SessionStore(this)
        if (session.serverUrl == null || !session.isSignedIn || !session.notificationsEnabled) return
        Thread { runCatching { JmailApi(session).registerDevice(session.installationId, token) } }.start()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (!SessionStore(this).notificationsEnabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CHANNEL, "New mail", NotificationManager.IMPORTANCE_DEFAULT),
            )
        }
        val data = message.data
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent ?: Intent(),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(data["sender"] ?: "New mail")
            .setContentText(data["subject"] ?: data["preview"] ?: "")
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
        manager.notify((data["messageId"] ?: System.currentTimeMillis().toString()).hashCode(), notification)
    }

    companion object { private const val CHANNEL = JmailApplication.NEW_MAIL_CHANNEL }
}
