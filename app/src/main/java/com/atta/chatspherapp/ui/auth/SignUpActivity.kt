package com.atta.chatspherapp.ui.auth

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.Camera
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.atta.chatspherapp.R
import com.atta.chatspherapp.databinding.ActivitySignUpBinding
import com.atta.chatspherapp.models.ContactModel
import com.atta.chatspherapp.ui.viewmodel.AuthViewModel
import com.atta.chatspherapp.ui.viewmodel.MainViewModel
import com.atta.chatspherapp.ui.viewmodel.StorageViewModel
import com.atta.chatspherapp.utils.Constants.COUNTRYNAMECODE
import com.atta.chatspherapp.utils.NewUtils.checkEditTexts
import com.atta.chatspherapp.utils.NewUtils.pickImageFromGallery
import com.atta.chatspherapp.utils.NewUtils.processPhoneNumber
import com.atta.chatspherapp.utils.NewUtils.setStatusBarColor
import com.atta.chatspherapp.utils.NewUtils.showProgressDialog
import com.atta.chatspherapp.utils.NewUtils.showToast
import com.atta.chatspherapp.utils.SharedPreferencesHelper
import com.bumptech.glide.Glide
import com.atta.chatspherapp.models.UserModel
import com.atta.chatspherapp.utils.Constants.CONTACTS
import com.atta.chatspherapp.utils.Constants.PHONE
import com.atta.chatspherapp.utils.Constants.USERS
import com.atta.chatspherapp.utils.NewUtils.compressImageUri
import com.atta.chatspherapp.utils.NewUtils.getBitmapFromFile
import com.atta.chatspherapp.utils.NewUtils.isValidEmail
import com.atta.chatspherapp.utils.NewUtils.onTextChange
import com.atta.chatspherapp.utils.NewUtils.openCamera
import com.atta.chatspherapp.utils.NewUtils.setAnimationOnView
import com.atta.chatspherapp.utils.NewUtils.showErrorToast
import com.atta.chatspherapp.utils.NewUtils.showSoftKeyboard
import com.atta.chatspherapp.utils.NewUtils.showSuccessToast
import com.atta.chatspherapp.utils.NewUtils.startNewActivity
import com.atta.chatspherapp.utils.NewUtils.startNewActivityFinishPreviousAll
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors
import javax.inject.Inject

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    lateinit var binding: ActivitySignUpBinding
    @Inject
    lateinit var mainViewModel: MainViewModel
    @Inject
    lateinit var authViewModel: AuthViewModel
    @Inject
    lateinit var storageViewModel: StorageViewModel
    @Inject
    lateinit var databaseReference: DatabaseReference
    @Inject
    lateinit var auth:FirebaseAuth
    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarColor(R.color.green)
        overridePendingTransition(R.anim.slide_in_bottom,R.anim.slide_out_bottom)
        animations()

        binding.loginTv.setOnClickListener {
            finish()
        }

        binding.backArrow.setOnClickListener {
            backRevealViews()
        }

        binding.cameraBtn.setOnClickListener {
            lifecycleScope.launch {
                binding.pd.visibility=View.VISIBLE
                binding.lottieAnimationView.visibility=View.VISIBLE
                binding.card.visibility=View.VISIBLE
                binding.previewViewCard.visibility=View.GONE
                binding.previewView.visibility=View.GONE
                binding.cameraBtn.visibility=View.GONE
                binding.toolBar.visibility=View.GONE
            }
        }


        binding.fullNameEdt.onTextChange {
            binding.fullNameEdt.error= null
        }
        binding.emailEdt.onTextChange {
            binding.emailEdt.error= null
        }
        binding.passwordEdt.onTextChange {
            binding.passwordEdt.error= null
        }


        binding.registerBtn.setOnClickListener {

            val name=binding.fullNameEdt.text.toString().trim()
            val email=binding.emailEdt.text.toString().trim()
            val password=binding.passwordEdt.text.toString().trim()

            if (name.isEmpty()||name.isBlank()){
                binding.fullNameEdt.error="Enter name"
                binding.fullNameEdt.showSoftKeyboard()
            }else if (email.isEmpty()||email.isBlank()){
                binding.emailEdt.error="Enter email"
                binding.fullNameEdt.error= null
                binding.emailEdt.showSoftKeyboard()
            }else if (password.isEmpty()||password.isBlank()){
                binding.passwordEdt.error="Enter password"
                binding.emailEdt.error=null
                binding.fullNameEdt.error= null
                binding.passwordEdt.showSoftKeyboard()
            }else if (password.length<=6){
                binding.passwordEdt.error="Password length must be greater then 6"
                binding.fullNameEdt.error= null
                binding.passwordEdt.showSoftKeyboard()
            }else if (!email.isValidEmail()){
                binding.emailEdt.error="Enter valid email"
                binding.fullNameEdt.error= null
                binding.emailEdt.showSoftKeyboard()
            }else{
                lifecycleScope.launch {
                    val progress= showProgressDialog("Register...")
                    val registerResult=authViewModel.registerWithEmailAndPassword(email, password)

                    val model=UserModel(fullName = name, email = email, password = password , timeStamp = System.currentTimeMillis())

                    registerResult.whenSuccess {authResult->

                        val authKey=auth.currentUser!!.uid
                        model.key=authKey

                        lifecycleScope.launch {
                            val uploadResult=mainViewModel.uploadAnyModel(USERS,model)
                            uploadResult.whenSuccess {
                                progress.dismiss()
                                startNewActivityFinishPreviousAll(SignInActivity::class.java)
                                showSuccessToast(authResult)
                            }
                            uploadResult.whenError {
                                showErrorToast(it.message.toString())
                                progress.dismiss()
                            }
                        }
                    }

                    registerResult.whenError {
                        showErrorToast(it.message.toString())
                        progress.dismiss()
                    }

                }
            }
        }

    }


    override fun onBackPressed() {
        if (binding.lottieAnimationView.visibility==View.GONE){
            backRevealViews()
        }else{
            super.onBackPressed()
        }
    }

    fun backRevealViews(){
        binding.lottieAnimationView.visibility=View.VISIBLE
        binding.card.visibility=View.VISIBLE
        binding.previewViewCard.visibility=View.GONE
        binding.cameraBtn.visibility=View.GONE
        binding.toolBar.visibility=View.GONE
    }


    fun animations(){
        binding.card.setAnimationOnView(R.anim.slide_up,800)
        binding.imgConstraint.setAnimationOnView(R.anim.slide_up,900)
        binding.nameInputLayout.setAnimationOnView(R.anim.slide_up,1000)
        binding.emailInputLayout.setAnimationOnView(R.anim.slide_up,1100)
        binding.passwordInputLayout.setAnimationOnView(R.anim.slide_up,1200)
        binding.registerBtn.setAnimationOnView(R.anim.slide_up,1300)
        binding.aleardyAccountLinear.setAnimationOnView(R.anim.slide_up,1400)
    }

}