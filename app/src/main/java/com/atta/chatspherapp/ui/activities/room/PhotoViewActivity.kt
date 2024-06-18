package com.atta.chatspherapp.ui.activities.room

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.atta.chatspherapp.R
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView

class PhotoViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_photo_view)
        val imageView = findViewById<PhotoView>(R.id.photoView)
        val image = intent.getStringExtra("image")
        Glide.with(this@PhotoViewActivity).load(image).into(imageView)
    }
}