package com.atta.chatspherapp.ui.activities.room

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import com.atta.chatspherapp.databinding.ActivityVideosBinding
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import com.atta.chatspherapp.utils.NewUtils.showToast

class VideosActivity : AppCompatActivity() {
    private lateinit var player: ExoPlayer
    private lateinit var videoUrl: String
    private var playbackPosition: Long = 0
    private lateinit var binding: ActivityVideosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        videoUrl = intent.getStringExtra("videoUrl")!!
        Log.i("onCreate", "onCreate:$videoUrl")

        if (::videoUrl.isInitialized) {
            initializePlayer(videoUrl)
        } else {
            Toast.makeText(this@VideosActivity, "Video is missing.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        player.seekTo(playbackPosition)
        player.play()
    }

    override fun onBackPressed() {
        player.release()
        finish()
        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        if (player.isPlaying) {
            playbackPosition = player.currentPosition
            player.pause()
        }
    }

    override fun onStop() {
        super.onStop()
        player.release()
    }

    @OptIn(UnstableApi::class)
    private fun initializePlayer(videoUrl: String) {
        try {
            Toast.makeText(this@VideosActivity, "Preparing", Toast.LENGTH_SHORT).show()
            player = ExoPlayer.Builder(this).setTrackSelector(DefaultTrackSelector(this)).build()
            val mediaItem = MediaItem.fromUri(videoUrl)
            player.setMediaItem(mediaItem)
            binding.playerView.player = player
            player.prepare()
            player.play()
        } catch (e: Exception) {
            showToast(e.message.toString())
        }
    }
}
