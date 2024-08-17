package com.atta.chatspherapp.ui.activities.room

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.atta.chatspherapp.R
import com.atta.chatspherapp.utils.NewUtils.loadImageViaLink
import com.atta.chatspherapp.utils.NewUtils.setAnimationOnView
import com.atta.chatspherapp.utils.NewUtils.setStatusBarColor
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView


class PhotoViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_view)
        setStatusBarColor(R.color.black)
        val photoView = findViewById<PhotoView>(R.id.photoView)
        val url = intent.getStringExtra("image")
        if (url!=null){
            photoView.loadImageViaLink(url, com.google.firebase.inappmessaging.display.R.drawable.image_placeholder)
            photoView.setAnimationOnView(R.anim.scale,400)
        }
    }
}