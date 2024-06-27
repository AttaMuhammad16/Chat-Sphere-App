package com.atta.chatspherapp.service

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import com.atta.chatspherapp.data.main.MainRepositoryImpl
import com.atta.chatspherapp.managers.NotificationManager.Companion.showNotification
import com.atta.chatspherapp.ui.activities.MainActivity
import com.atta.chatspherapp.utils.Constants
import com.atta.chatspherapp.utils.Constants.FCMTOENNODE
import com.atta.chatspherapp.utils.InternetChecker
import com.atta.chatspherapp.utils.MyExtensions
import com.atta.chatspherapp.utils.NewUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject


@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var database: DatabaseReference
    @Inject
    lateinit var auth: FirebaseAuth

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data=remoteMessage.data
        remoteMessage.notification?.let {
            showNotifications(it.title ?: "stable version", it.body ?: "stable version")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = auth.currentUser
                if (user != null) {
                    if (InternetChecker().isInternetConnectedWithPackage(this@MyFirebaseMessagingService)){
                        val map = HashMap<String, Any>()
                        map[FCMTOENNODE] = token
                        database.child(Constants.USERS + "/" + user.uid).updateChildren(map).await()
                        Log.i("TAG", "Token successfully uploaded")
                    }
                } else {
                    Log.w("TAG", "User is not authenticated, cannot upload token")
                }
            } catch (e: Exception) {
                Log.e("TAG", "Failed to upload token", e)
            }
        }
    }


    private fun showNotifications(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 4, intent, PendingIntent.FLAG_IMMUTABLE)
        showNotification(System.currentTimeMillis().toInt(), "Chat Sphere App", pendingIntent, this, title, messageBody)
    }



}