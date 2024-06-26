package com.atta.chatspherapp.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.atta.chatspherapp.R
import com.atta.chatspherapp.data.main.MainRepository
import com.atta.chatspherapp.models.MessageModel
import com.atta.chatspherapp.models.UserModel
import com.atta.chatspherapp.ui.activities.room.ChatActivity
import com.atta.chatspherapp.utils.MyResult
import com.atta.chatspherapp.utils.SharedPreferencesHelper
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class UploadImageService : Service() {

    private val NOTIFICATION_CHANNEL_ID = "Image"
    private val NOTIFICATION_ID = 1234

    private val binder = LocalBinder()
    private lateinit var notificationManager: NotificationManager
    private lateinit var builder: NotificationCompat.Builder

    @Inject
    lateinit var storageReference: StorageReference
    @Inject
    lateinit var mainRepository: MainRepository

    lateinit var preferencesHelper: SharedPreferencesHelper

    inner class LocalBinder : Binder() {
        fun getService(): UploadImageService = this@UploadImageService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()
        createInitialNotification()
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createInitialNotification())
        preferencesHelper= SharedPreferencesHelper(this)


        if (intent?.action == "Cancel") {
            stopSelf()
            deleteNotification(notificationManager)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val uri = intent!!.getStringExtra("imageUri")
            if (uri!!.isNotEmpty()) {

                val imageUploadPath = intent.getStringExtra("chatUploadPath")
                val senderUid = intent.getStringExtra("userUid")
                val time = intent.getLongExtra("time",0)
                val key = intent.getStringExtra("key")!!

                preferencesHelper.saveString(key,uri)

                val result = uploadImageToFirebaseStorage(Uri.parse(uri)) {
                    updateNotification(it.toString(), "Image")
                }

                result.whenError {
                    Log.i("TAG", "onStartCommand:$it")
                    stopSelf()
                    deleteNotification(notificationManager)
                }

                result.whenSuccess {

                    val messageModel = MessageModel(
                        key=key,
                        senderName = "",
                        senderImageUrl = "",
                        senderPhone = "",
                        timeStamp = time,
                        senderUid = senderUid!!,
                        imageUrl = it,
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        val uploadResult = mainRepository.uploadAnyModel(imageUploadPath!!, messageModel)
                        uploadResult.whenError {
                            Log.i("TAG", "onStartCommand:$it")
                        }
                        uploadResult.whenSuccess {
                            stopSelf()
                            deleteNotification(notificationManager)
                        }
                    }
                }

            }else{
                delay(2000)
                stopSelf()
                deleteNotification(notificationManager)
                Toast.makeText(this@UploadImageService, "Uri is empty.", Toast.LENGTH_SHORT).show()
            }
        }
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Image",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createInitialNotification(): Notification {
        val notificationIntent = Intent(this, ChatActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 1, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val cancelIntent = Intent(this, UploadImageService::class.java).apply {
            action = "Cancel"
        }

        val cancelPendingIntent = PendingIntent.getService(this, 1, cancelIntent, PendingIntent.FLAG_IMMUTABLE)

        builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Sending Image..")
            .setSmallIcon(R.drawable.alarm)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setOngoing(true)
            .addAction(R.drawable.cancel,"Cancel",cancelPendingIntent)
        notificationManager.notify(NOTIFICATION_ID, builder.build())
        return builder.build()
    }

    private fun updateNotification(percent: String, contentText: String) {
        builder.apply {
            if (percent.toInt()==100){
                setContentTitle("Image sent successfully")
            }
            setContentTitle("Sending Image")
            setContentText("$percent % $contentText")
            setSmallIcon(R.drawable.alarm)
            setAutoCancel(true)
            setOngoing(true)
            setProgress(100,percent.toInt(),false)
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun deleteNotification(mNotificationManager: NotificationManager) {
        mNotificationManager.cancel(NOTIFICATION_ID)
        stopSelf()
    }


    suspend fun uploadImageToFirebaseStorage(uri: Uri, percentage: (Int) -> Unit): MyResult<String> {
        return try {
            val uploadTask = storageReference.child(System.currentTimeMillis().toString()).putFile(uri)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = ((100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount).toInt()
                percentage(progress)
            }

            val result = uploadTask.await()
            val downloadUrl = result.storage.downloadUrl.await()
            MyResult.Success(downloadUrl.toString())
        } catch (e: Exception) {
            MyResult.Error(e.message ?: "Unknown error occurred")
        }
    }



}