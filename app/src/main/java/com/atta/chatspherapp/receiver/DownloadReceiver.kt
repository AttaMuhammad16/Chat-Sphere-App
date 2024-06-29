package com.atta.chatspherapp.receiver

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.atta.chatspherapp.adapters.ChatAdapter.Companion.videoUri


class DownloadReceiver : BroadcastReceiver() {
    @SuppressLint("Range")
    override fun onReceive(context: Context, intent: Intent) {
        if(intent.action==DownloadManager.ACTION_DOWNLOAD_COMPLETE){
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (downloadId != -1L) {
                val uri= downloadManager.getUriForDownloadedFile(downloadId).toString()
                videoUri(uri,context)
            } else {
                Toast.makeText(context, "Download id is null.", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(context, "Download Not completed yet.", Toast.LENGTH_SHORT).show()
        }
    }
}