package com.atta.chatspherapp.managers

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.atta.chatspherapp.R
import com.atta.chatspherapp.ui.activities.MainActivity

class MyNotificationManager(private val context: Context , requestPermissionLauncher: ActivityResultLauncher<String>?=null) {

    init {
        if (requestPermissionLauncher!=null){
            askNotificationPermission(context,requestPermissionLauncher)
        }
    }

    fun askNotificationPermission(context: Context, requestPermissionLauncher: ActivityResultLauncher<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                }
                context is Activity && context.shouldShowRequestPermissionRationale( android.Manifest.permission.POST_NOTIFICATIONS) -> {
                    requestPermissionLauncher.launch( android.Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch( android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

}