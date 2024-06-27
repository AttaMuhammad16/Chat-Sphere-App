package com.atta.chatspherapp.managers

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.atta.chatspherapp.R


class NotificationManager {

    companion object {
        private const val GROUP_KEY_CHAT = "com.chatspherapp.CHAT"
        private const val SUMMARY_ID = 0
        private val messages = mutableListOf<String>()

        @SuppressLint("MissingPermission")
        fun showNotification(id: Int, channelId: String, pendingIntent: PendingIntent?, context: Context, title: String, message: String) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, "Chat Sphere Messages Notification", NotificationManager.IMPORTANCE_HIGH)
                notificationManager.createNotificationChannel(channel)
            }
            // Add the new message to the list
            messages.add("""
               $title
               $message 
            """.trimIndent())

            // Create the summary notification
            val inboxStyle = NotificationCompat.InboxStyle()
            for (msg in messages) {
                inboxStyle.addLine(msg)
            }
            inboxStyle.setSummaryText("${messages.size} new messages")

            val summaryNotification = NotificationCompat.Builder(context, channelId)
                .setContentTitle("Chat Sphere")
                .setContentText("You have new messages")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setStyle(inboxStyle)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setGroup(GROUP_KEY_CHAT)
                .setGroupSummary(true)
                .build()

            // Notify the summary notification
            NotificationManagerCompat.from(context).notify(SUMMARY_ID, summaryNotification)
        }

        fun clearNotifications(context: Context) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancelAll()
            messages.clear()
        }

    }
}
