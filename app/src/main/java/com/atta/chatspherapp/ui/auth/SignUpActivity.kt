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

    val requestCodeForCamera=12
    lateinit var profileUri:Uri

    private lateinit var imageCapture: ImageCapture

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarColor(R.color.green)
        overridePendingTransition(R.anim.slide_in_bottom,R.anim.slide_out_bottom)
        animations()

        requestPermissions()

        binding.loginTv.setOnClickListener {
            finish()
        }

        binding.selectedImg.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                checkAndOpenCamera()
            }else{
                showErrorToast("Camera permission is not granted")
            }
        }

        binding.selectUserImage.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
                checkAndOpenCamera()
            }else{
                showErrorToast("Camera permission is not granted")
            }
        }

        binding.backArrow.setOnClickListener {
            backRevealViews()
        }

        binding.cameraBtn.setOnClickListener {
            lifecycleScope.launch {
                binding.pd.visibility=View.VISIBLE
                captureImage()
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
                            if (::profileUri.isInitialized){
                                val imgUrlResult=storageViewModel.uploadImageToFirebaseStorage(profileUri.toString())
                                imgUrlResult.whenSuccess {profileUrl->
                                    model.profileUrl=profileUrl
                                    lifecycleScope.launch {
                                        val uploadResult=mainViewModel.uploadAnyModel(USERS,model)
                                        uploadResult.whenSuccess {
                                            startNewActivityFinishPreviousAll(SignInActivity::class.java)
                                            showSuccessToast(authResult)
                                        }
                                        uploadResult.whenError {
                                            showErrorToast(it.message.toString())
                                            progress.dismiss()
                                        }
                                    }
                                }
                                imgUrlResult.whenError {
                                    showErrorToast(it.message.toString())
                                    progress.dismiss()
                                }
                            }else{
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
                    }

                    registerResult.whenError {
                        showErrorToast(it.message.toString())
                        progress.dismiss()
                    }

                }
            }
        }

    }

    fun checkAndOpenCamera(){
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            binding.lottieAnimationView.visibility=View.GONE
            binding.card.visibility=View.GONE
            binding.previewViewCard.visibility=View.VISIBLE
            binding.previewView.visibility=View.VISIBLE
            binding.cameraBtn.visibility=View.VISIBLE
            binding.toolBar.visibility=View.VISIBLE
            binding.previewViewCard.setAnimationOnView(R.anim.scale,1000)
            binding.cameraBtn.setAnimationOnView(R.anim.scale,1200)
            startCamera()
        }else{
            showErrorToast("Front camera not found")
        }
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA,Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE), 1221)
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }

            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT) // Use CameraSelector.LENS_FACING_BACK for back camera
                .build()

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.e("CameraActivity", "Use case binding failed", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private suspend fun captureImage() {
        if (::imageCapture.isInitialized){
            val photoFile = createFile()
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
            imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    lifecycleScope.launch {
                        val savedUri = Uri.fromFile(photoFile)
                        val compressUri=compressImageUri(savedUri,50)
                        if (compressUri!=null){
                            binding.selectedImg.setImageURI(savedUri)
                            binding.pd.visibility=View.GONE
                            profileUri=compressUri
                        }
                        releaseCamera()
                    }
                }
                override fun onError(exception: ImageCaptureException) {
                    showErrorToast(exception.message.toString())
                }
            })
        }else{
            showErrorToast("Camera is not ready.Try again")
        }
    }

    private suspend fun createFile(): File {
        val photoFile = File(getOutputDirectory(), "${System.currentTimeMillis()}.jpg")
        return photoFile
    }

    private suspend fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return mediaDir ?: filesDir
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
        releaseCamera()
    }


    fun releaseCamera(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
        }, ContextCompat.getMainExecutor(this))
    }




    fun animations(){
        binding.card.setAnimationOnView(R.anim.slide_up,2000)
        binding.imgConstraint.setAnimationOnView(R.anim.slide_up,2200)
        binding.nameInputLayout.setAnimationOnView(R.anim.slide_up,2400)
        binding.emailInputLayout.setAnimationOnView(R.anim.slide_up,2600)
        binding.passwordInputLayout.setAnimationOnView(R.anim.slide_up,2800)
        binding.registerBtn.setAnimationOnView(R.anim.slide_up,3000)
        binding.aleardyAccountLinear.setAnimationOnView(R.anim.slide_up,3200)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestCodeForCamera) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkAndOpenCamera()
            }
        }
    }

}