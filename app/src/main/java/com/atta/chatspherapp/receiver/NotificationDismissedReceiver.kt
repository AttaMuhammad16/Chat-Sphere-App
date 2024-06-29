package com.atta.chatspherapp.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.atta.chatspherapp.managers.NotificationManager


class NotificationDismissedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
       val data=intent?.getStringExtra("isFromNotification")
       if (data=="yes"){
           NotificationManager.clearNotifications(context!!)
       }
    }
}