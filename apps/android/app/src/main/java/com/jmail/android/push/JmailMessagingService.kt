package com.jmail.android.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jmail.android.data.JmailApi
import com.jmail.android.data.SessionStore

class JmailMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        val session = SessionStore(this)
        if (session.serverUrl == null || session.accessToken == null) return
        val installationId = getSharedPreferences("jmail_session", MODE_PRIVATE)
            .getString("installation_id", null) ?: return
        Thread { runCatching { JmailApi(session).registerDevice(installationId, token) } }.start()
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "New mail", NotificationManager.IMPORTANCE_DEFAULT))
        val data = message.data
        val notification = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(data["sender"] ?: "New mail")
            .setContentText(data["subject"] ?: data["preview"] ?: "")
            .setAutoCancel(true)
            .build()
        manager.notify((data["messageId"] ?: System.currentTimeMillis().toString()).hashCode(), notification)
    }

    companion object { private const val CHANNEL = "new_mail" }
}
