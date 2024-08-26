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
import com.atta.chatspherapp.utils.Constants.DELETEMESSAGEFROMME
import com.atta.chatspherapp.utils.Constants.DELETEMESSAGELIST
import com.atta.chatspherapp.utils.Constants.REACTIONDETAILS
import com.atta.chatspherapp.utils.Constants.RECENTCHAT
import com.atta.chatspherapp.utils.Constants.ROOM
import com.atta.chatspherapp.utils.NewUtils.getSortedKeys
import com.atta.chatspherapp.utils.NewUtils.showErrorToast
import com.atta.chatspherapp.utils.NewUtils.showToast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
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
import kotlinx.coroutines.tasks.await
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

    @Inject
    lateinit var databaseReference: DatabaseReference


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
            val listOfLinks = mutableListOf<String>()

            recentMessagesList?.let { messagesList ->
                val deleteResults = messagesList.map { model ->
                    async {
                        val result = mainRepository.deleteAnyModel("$RECENTCHAT/$mykey/${model.userModel.key}")
                        result.whenError {
                            Log.i("Deletion", "Deletion error: ${it.message}")
                            showToast(it.message.toString())
                        }
                    }
                }
                deleteResults.awaitAll()
            }

            val messagesListAwait = recentMessagesList?.map { model ->

                val roomSortedKey = getSortedKeys(model.key, mykey!!)

                return@map if (model.userModel.key==mykey){

                    async {

                        val roomMessagesList = mainRepository.getModelsList("$ROOM/$roomSortedKey", MessageModel::class.java)

                        roomMessagesList.whenSuccess { messages ->
                            messages.forEach { messageModel ->
                                messageModel.imageUrl.takeIf { it.isNotEmpty() }?.let { listOfLinks.add(it) }
                                messageModel.voiceUrl.takeIf { it.isNotEmpty() }?.let { listOfLinks.add(it) }
                                messageModel.documentUrl.takeIf { it.isNotEmpty() }?.let { listOfLinks.add(it) }
                                messageModel.videoUrl.takeIf { it.isNotEmpty() }?.let { listOfLinks.add(it) }
                            }
                        }

                        mainRepository.deleteAnyModel("$ROOM/$roomSortedKey")
                        mainRepository.deleteAnyModel("$REACTIONDETAILS/$roomSortedKey")

                    }

                }else{

                    val isExists = async {
                        try {
                            mainRepository.checkChildExists("$RECENTCHAT/${model.userModel.key}/$mykey")
                        } catch (e: Exception) {
                            Log.e("exists", "Error checking if child exists: ${e.message}")
                            false
                        }
                    }.await()


                    if (!isExists) {
                        async {

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
                                Log.e("Fetching", "Fetching messages error: ${it.message}")
                                showToast(it.message.toString())
                            }

                            mainRepository.deleteAnyModel("$ROOM/$roomSortedKey")
                            mainRepository.deleteAnyModel("$REACTIONDETAILS/$roomSortedKey")

                        }

                    } else {
                        val roomMessagesList = mainRepository.getModelsList("$ROOM/$roomSortedKey", MessageModel::class.java)

                        roomMessagesList.whenSuccess { messageModels ->
                            val updatesMap = mutableMapOf<String, Any>()

                            messageModels.forEach { messagemodel ->
                                messagemodel.deletedMessagesList.add(mykey!!)
                                val pathBol = "$ROOM/$roomSortedKey/${messagemodel.key}/$DELETEMESSAGEFROMME"
                                val pathList = "$ROOM/$roomSortedKey/${messagemodel.key}/$DELETEMESSAGELIST"
                                updatesMap[pathBol] = true
                                updatesMap[pathList] = messagemodel.deletedMessagesList
                            }
                            Log.i("TAG", "startDeleting: $updatesMap")

                            if (updatesMap.isNotEmpty()) {
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        databaseReference.updateChildren(updatesMap).await()
                                        Log.d("Batch", "Batch update successful")
                                    } catch (e: Exception) {
                                        Log.e("Batch", "Batch update failed: ${e.message}")
                                    }
                                }
                            }
                        }
                        null
                    }

                }

            }

            messagesListAwait?.filterNotNull()?.awaitAll()

            if (listOfLinks.isNotEmpty()) {
                listOfLinks.chunked(10).forEach { chunk ->
                    chunk.map { link ->
                        async {
                            try {
                                storageRepository.deleteDocumentToFirebaseStorage(link)
                            } catch (e: Exception) {
                                Log.e("deleting", "Error deleting link: ${e.message}")
                            }
                        }
                    }.awaitAll()
                }
            }

            deleteNotification(notificationManager)
            stopSelf()
            serviceScope.cancel()

        }
    }



}