package com.atta.chatspherapp.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.atta.chatspherapp.R
import com.atta.chatspherapp.ui.activities.recentchat.MainActivity
import com.atta.chatspherapp.ui.auth.SignInActivity
import com.atta.chatspherapp.utils.InternetChecker
import com.atta.chatspherapp.utils.NewUtils.setAnimationOnView
import com.atta.chatspherapp.utils.NewUtils.setStatusBarColor
import com.atta.chatspherapp.utils.NewUtils.startNewActivity
import com.google.android.gms.tasks.Task
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashScreen : AppCompatActivity() {

    @Inject
    lateinit var auth: FirebaseAuth

    private val REQUEST_CODE = 1001
    private lateinit var appUpdateManager: AppUpdateManager
    private lateinit var appUpdateInfoTask: Task<AppUpdateInfo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        setStatusBarColor(R.color.green)

        val splashText = findViewById<TextView>(R.id.splashText)
        splashText.setAnimationOnView(R.anim.slide_up, 1500)

        // Initialize AppUpdateManager
        appUpdateManager = AppUpdateManagerFactory.create(this)
        appUpdateInfoTask = appUpdateManager.appUpdateInfo
        lifecycleScope.launch {
            if (!InternetChecker().isInternetConnectedWithPackage(this@SplashScreen)) {
                proceedToNextActivity()
            }else{
                checkForUpdate()
            }
        }
    }


    private fun checkForUpdate() {
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            when {
                appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                        appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE) -> {
                    // Start the update flow
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        this,
                        REQUEST_CODE
                    )
                }
                else -> {
                    // No update available, proceed to the next activity
                    proceedToNextActivity()
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("Failed", "Update check failed", exception)
            proceedToNextActivity() // Proceed even if update check fails
        }
    }

    override fun onResume() {
        super.onResume()
        resumeUpdateIfNeeded()
    }

    private fun resumeUpdateIfNeeded() {
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,
                    REQUEST_CODE
                )
            }
        }.addOnFailureListener { exception ->
            Log.e("Failed", "Resuming update failed", exception)
            proceedToNextActivity() // Proceed if resuming update fails
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Update completed, proceed to the MainActivity
                startNewActivity(MainActivity::class.java, true)
            } else {
                Log.e("Failed", "Update flow failed or canceled")
                proceedToNextActivity()
            }
        }
    }

    private fun proceedToNextActivity() {
        lifecycleScope.launch {
            delay(3000)
            val nextActivity = if (auth.currentUser != null) MainActivity::class.java else SignInActivity::class.java
            startNewActivity(nextActivity, true)
        }
    }


}