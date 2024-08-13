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
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.atta.chatspherapp.R
import com.atta.chatspherapp.data.main.MainRepository
import com.atta.chatspherapp.models.MessageModel
import com.atta.chatspherapp.models.UserModel
import com.atta.chatspherapp.ui.activities.room.ChatActivity
import com.atta.chatspherapp.utils.MyResult
import com.atta.chatspherapp.utils.SharedPreferencesHelper
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@AndroidEntryPoint
class UploadDocumentService : Service() {

    private val NOTIFICATION_CHANNEL_ID = "document"
    private val NOTIFICATION_ID = 8614

    private val binder = LocalBinder()
    private lateinit var notificationManager: NotificationManager
    private lateinit var builder: NotificationCompat.Builder

    @Inject
    lateinit var storageReference: StorageReference
    @Inject
    lateinit var mainRepository: MainRepository
    lateinit var preferencesHelper: SharedPreferencesHelper
    @Inject
    lateinit var databaseReference: DatabaseReference


    inner class LocalBinder : Binder() {
        fun getService(): UploadDocumentService = this@UploadDocumentService
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
        preferencesHelper= SharedPreferencesHelper(this)
        startForeground(NOTIFICATION_ID, createInitialNotification())
        if (intent?.action == "Cancel") {
            stopSelf()
            deleteNotification(notificationManager)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val uri = intent!!.getStringExtra("docxUri")
            if (uri!!.isNotEmpty()) {
                val documentUploadPath = intent.getStringExtra("chatUploadPath")
                val senderUid = intent.getStringExtra("userUid")
                val fileName = intent.getStringExtra("docxFileName")
                val key = intent.getStringExtra("key")!!
                val time = intent.getLongExtra("time",0)

                val result = uploadDocumentToFirebaseStorage(Uri.parse(uri)) {
                    updateNotification(it.toString(), fileName!!)
                }

                result.whenError {
                    Toast.makeText(this@UploadDocumentService, it.message, Toast.LENGTH_SHORT).show()
                    stopSelf()
                    deleteNotification(notificationManager)
                }

                result.whenSuccess {

                    val messageModel = MessageModel(
                        key = key,
                        senderName = "",
                        senderImageUrl = "",
                        senderPhone = "",
                        timeStamp = time,
                        senderUid = senderUid!!,
                        documentUrl = it,
                        documentFileName = fileName!!
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        val uploadResult = mainRepository.uploadAnyModel(documentUploadPath!!, messageModel)
                        preferencesHelper.saveString(key,uri.toString())

                        uploadResult.whenError {
                            Toast.makeText(this@UploadDocumentService, "${it.message}", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@UploadDocumentService, "Uri is empty", Toast.LENGTH_SHORT).show()
            }
        }
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "document",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createInitialNotification(): Notification {
        val notificationIntent = Intent(this, ChatActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 2, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val cancelIntent = Intent(this, UploadDocumentService::class.java).apply {
            action = "Cancel"
        }

        val cancelPendingIntent = PendingIntent.getService(this, 2, cancelIntent, PendingIntent.FLAG_IMMUTABLE)

        builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Sending Document..")
            .setSmallIcon(R.drawable.app_icon)
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
                setContentTitle("Document sent successfully")
            }
            setContentTitle(contentText)
            setContentText("$percent % uploaded")
            setSmallIcon(R.drawable.app_icon)
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

    suspend fun uploadDocumentToFirebaseStorage(uri: Uri, percentage: (Int) -> Unit): MyResult<String> {
        return try {
            val uploadTask = storageReference.child("documents/${System.currentTimeMillis()}").putFile(uri)

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