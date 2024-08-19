package com.atta.chatspherapp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.atta.chatspherapp.R
import com.atta.chatspherapp.data.main.MainRepository
import com.atta.chatspherapp.data.storage.StorageRepository
import com.atta.chatspherapp.models.MessageModel
import com.atta.chatspherapp.models.RecentChatModel
import com.atta.chatspherapp.ui.activities.recentchat.MainActivity
import com.atta.chatspherapp.utils.Constants.REACTIONDETAILS
import com.atta.chatspherapp.utils.Constants.RECENTCHAT
import com.atta.chatspherapp.utils.Constants.ROOM
import com.atta.chatspherapp.utils.NewUtils.getSortedKeys
import com.atta.chatspherapp.utils.NewUtils.showToast
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class DeleteMessagesService : Service() {

    @Inject
    lateinit var mainRepository: MainRepository
    @Inject
    lateinit var storageRepository: StorageRepository

    private val NOTIFICATION_CHANNEL_ID = "Deleting Message Channel"
    private val NOTIFICATION_ID = 30801

    @Inject
    lateinit var auth: FirebaseAuth

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)



    private val binder = LocalBinder()
    private lateinit var notificationManager: NotificationManager
    private lateinit var builder: NotificationCompat.Builder

    inner class LocalBinder : Binder() {
        fun getService(): DeleteMessagesService = this@DeleteMessagesService
    }

    var mykey:String?=""

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        createInitialNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createInitialNotification())
        val recentMessagesList: ArrayList<RecentChatModel>? = intent?.getParcelableArrayListExtra("selectedMessages")
        mykey=auth.currentUser?.uid
        serviceScope.launch {
            startDeleting(recentMessagesList)
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Deleting Message",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createInitialNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("deleting messages ...")
            .setSmallIcon(R.drawable.app_icon)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
            .setProgress(0, 0, true)
        notificationManager.notify(NOTIFICATION_ID, builder.build())
        return builder.build()
    }

    private fun deleteNotification(mNotificationManager: NotificationManager) {
        mNotificationManager.cancel(NOTIFICATION_ID)
    }

    private suspend fun startDeleting(recentMessagesList: ArrayList<RecentChatModel>?) {

        withContext(Dispatchers.IO) {

            recentMessagesList?.let { messagesList ->
                val deleteResults = messagesList.map { model ->
                    async {
                        val result = mainRepository.deleteAnyModel("$RECENTCHAT/$mykey/${model.userModel.key}")
                        result.whenError {
                            Log.i("TAG", "Deletion error: ${it.message}")
                            showToast(it.message.toString())
                        }
                    }
                }
                deleteResults.awaitAll()
            }

            val listOfLinks = mutableListOf<String>()
            val linkResults = recentMessagesList?.map { model ->
                async {
                    val roomSortedKey = getSortedKeys(model.userModel.key, mykey!!)
                    val roomMessagesList = mainRepository.getModelsList("$ROOM/$roomSortedKey", MessageModel::class.java)
                    roomMessagesList.whenSuccess { messages ->
                        messages.forEach { messageModel ->
                            messageModel.imageUrl.takeIf { it.isNotEmpty() }?.let { listOfLinks.add(it) }
                            messageModel.voiceUrl.takeIf { it.isNotEmpty() }?.let { listOfLinks.add(it) }
                            messageModel.documentUrl.takeIf { it.isNotEmpty() }?.let { listOfLinks.add(it) }
                            messageModel.videoUrl.takeIf { it.isNotEmpty() }?.let { listOfLinks.add(it) }
                        }
                    }
                    roomMessagesList.whenError {
                        Log.i("TAG", "Fetching messages error: ${it.message}")
                        showToast(it.message.toString())
                    }
                    mainRepository.deleteAnyModel("$ROOM/$roomSortedKey")
                    mainRepository.deleteAnyModel("$REACTIONDETAILS/$roomSortedKey")
                }
            }

            linkResults?.awaitAll()

            listOfLinks.chunked(10).forEach { chunk ->
                chunk.map { link ->
                    async { storageRepository.deleteDocumentToFirebaseStorage(link) }
                }.awaitAll()
            }

            deleteNotification(notificationManager)
            stopSelf()
            serviceScope.cancel()
        }
    }
}