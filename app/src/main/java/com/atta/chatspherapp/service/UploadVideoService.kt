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
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import com.atta.chatspherapp.R
import com.atta.chatspherapp.data.main.MainRepository
import com.atta.chatspherapp.data.storage.StorageRepository
import com.atta.chatspherapp.models.MessageModel
import com.atta.chatspherapp.ui.activities.room.ChatActivity
import com.atta.chatspherapp.utils.NewUtils.updateFlow
import com.atta.chatspherapp.utils.NewUtils.uploadVideoToFirebaseStorage
import com.atta.chatspherapp.utils.SharedPreferencesHelper
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.UploadTask
import com.atta.chatspherapp.adapters.ChatAdapter
import com.atta.chatspherapp.models.UserModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class UploadVideoService : Service() {

    private val NOTIFICATION_CHANNEL_ID = "Video"
    private val NOTIFICATION_ID = 123


    private val binder = LocalBinder()
    private lateinit var notificationManager: NotificationManager
    private lateinit var builder: NotificationCompat.Builder

    @Inject
    lateinit var storageRepository: StorageRepository
    @Inject
    lateinit var mainRepository: MainRepository
    @Inject
    lateinit var databaseReference: DatabaseReference
    lateinit var preferencesHelper: SharedPreferencesHelper
    val LifecycleOwner.lifecycleScope: LifecycleCoroutineScope
        get() = lifecycle.coroutineScope
    lateinit var uploadTask: UploadTask

    inner class LocalBinder : Binder() {
        fun getService(): UploadVideoService = this@UploadVideoService
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
            deleteNotification(notificationManager)
            if (::uploadTask.isInitialized){
                uploadTask.cancel()
            }
        }

        val uri = intent!!.getStringExtra("videoUri")


        if (uri?.isNotEmpty()!=null) {

            val videoUploadPath = intent.getStringExtra("chatUploadPath")
            val senderUid = intent.getStringExtra("userUid")
            val key = intent.getStringExtra("key")!!
            val timestamp = intent.getLongExtra("timestamp",0)

            CoroutineScope(Dispatchers.IO).launch {

                val result = uploadVideoToFirebaseStorage(Uri.parse(uri),{
                    updateNotification(it.toString(), "Uploaded")
                    updateFlow(it)
                }) {
                    uploadTask=it
                }

                result.whenError {
                    stopSelf()
                    deleteNotification(notificationManager)
                }

                result.whenSuccess {

                    val messageModel = MessageModel(
                        key=key,
                        senderName = "",
                        senderImageUrl = "",
                        senderPhone = "",
                        timeStamp =timestamp ,
                        senderUid = senderUid!!,
                        videoUrl = it,
                    )

                    CoroutineScope(Dispatchers.Main).launch {
                        val uploadResult = mainRepository.uploadAnyModel(videoUploadPath!!, messageModel)
                        preferencesHelper.saveString(key,uri.toString())
                        ChatAdapter.refreshAdapter()
                        uploadResult.whenError {
                            Log.i("TAG", "onStartCommand:$it")
                        }
                        uploadResult.whenSuccess {
                            stopSelf()
                            deleteNotification(notificationManager)
                        }
                    }

                }
            }

        }else{
            stopSelf()
            deleteNotification(notificationManager)
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Video",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createInitialNotification(): Notification {
        val notificationIntent = Intent(this, ChatActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val cancelIntent = Intent(this, UploadVideoService::class.java).apply {
            action = "Cancel"
        }
        val cancelPendingIntent = PendingIntent.getService(this, 1, cancelIntent, PendingIntent.FLAG_IMMUTABLE)

        builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Sending Video..")
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
                setContentTitle("Video sent successfully")
            }
            setContentTitle("Sending Video")
            setContentText("$percent % $contentText")
            setSmallIcon(R.drawable.alarm)
            setAutoCancel(true)
            setOngoing(true)
            setProgress(100,percent.toInt(),false)
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    fun deleteNotification(mNotificationManager:NotificationManager) {
        mNotificationManager.cancel(NOTIFICATION_ID)
        stopSelf()
    }



}