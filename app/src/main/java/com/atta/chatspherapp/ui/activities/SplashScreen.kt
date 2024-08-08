package com.atta.chatspherapp.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.atta.chatspherapp.R
import com.atta.chatspherapp.ui.activities.recentchat.MainActivity
import com.atta.chatspherapp.ui.auth.SignInActivity
import com.atta.chatspherapp.utils.NewUtils.setAnimationOnView
import com.atta.chatspherapp.utils.NewUtils.setStatusBarColor
import com.atta.chatspherapp.utils.NewUtils.startNewActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashScreen : AppCompatActivity() {
    @Inject
    lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        setStatusBarColor(R.color.green)
        val splashText = findViewById<TextView>(R.id.splashText)

        splashText.setAnimationOnView(R.anim.slide_up,1500)
        Handler().postDelayed({
            if (auth.currentUser!=null){
                startNewActivity(MainActivity::class.java,true)
            }else{
                startNewActivity(SignInActivity::class.java,true)
            }
        },2500)



    }
}