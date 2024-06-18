package com.atta.chatspherapp.ui.activities.room

import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.atta.chatspherapp.databinding.ActivityVideosBinding
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector

class VideosActivity : AppCompatActivity() {
    private lateinit var player: SimpleExoPlayer
    lateinit var videoUrl: String
    private var playbackPosition: Long = 0
    lateinit var binding: ActivityVideosBinding
    lateinit var simpleExoPlayer: SimpleExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityVideosBinding.inflate(layoutInflater)
        setContentView(binding.root)


        simpleExoPlayer= SimpleExoPlayer.Builder(this).setTrackSelector(DefaultTrackSelector(this)).build()
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        videoUrl=intent.getStringExtra("videoUrl")!!
        Log.i("TAG", "onCreate:$videoUrl")

        if (::videoUrl.isInitialized){
            initializePlayer(videoUrl)
        }else{
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

    fun initializePlayer(videoUrl:String) {
        try {
            player = simpleExoPlayer
            val mediaItem = com.google.android.exoplayer2.MediaItem.fromUri(videoUrl)
            player.setMediaItem(mediaItem)
            binding.playerView.player = player
            player.prepare()
            player.play()
            Toast.makeText(this@VideosActivity, "Preparing", Toast.LENGTH_SHORT).show()
        }catch (e:Exception){
            Log.i("TAG", "initializePlayer: ${e.message} ")
        }
    }

}

















