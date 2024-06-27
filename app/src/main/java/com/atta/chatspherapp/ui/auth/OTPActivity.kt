package com.atta.chatspherapp.ui.auth

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.atta.chatspherapp.R
import com.atta.chatspherapp.databinding.ActivityOtpactivityBinding
import com.atta.chatspherapp.models.ContactModel
import com.atta.chatspherapp.ui.activities.MainActivity
import com.atta.chatspherapp.ui.viewmodel.AuthViewModel
import com.atta.chatspherapp.ui.viewmodel.MainViewModel
import com.atta.chatspherapp.ui.viewmodel.StorageViewModel
import com.atta.chatspherapp.utils.NewUtils.setStatusBarColor
import com.atta.chatspherapp.utils.NewUtils.showProgressDialog
import com.atta.chatspherapp.utils.NewUtils.showToast
import com.atta.chatspherapp.utils.NewUtils.startCountdownTimer
import com.atta.chatspherapp.models.UserModel
import com.atta.chatspherapp.utils.Constants.CONTACTS
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@AndroidEntryPoint
class OTPActivity : AppCompatActivity() {

    lateinit var binding: ActivityOtpactivityBinding
    private val waitingTime = 60 * 1000L
    private var countDownTimer: CountDownTimer? = null

    @Inject
    lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var authViewModel: AuthViewModel

    @Inject
    lateinit var storageViewModel: StorageViewModel


    private var userModel: UserModel?=null
    private var uri: String?=null
    private var verificationId = ""
    private lateinit var token: PhoneAuthProvider.ForceResendingToken
    private lateinit var crediential: PhoneAuthCredential
    private lateinit var mCallBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var bundlePhoneNumber:String?=null

    @Inject
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setStatusBarColor(R.color.black)
        countDownTimer = startCountdownTimer(waitingTime, binding.timerTv, this@OTPActivity,binding.resendTv)

        userModel = intent.getParcelableExtra("userModel")
        uri=intent.getStringExtra("uri")

        bundlePhoneNumber=intent.getStringExtra("phoneNumber")
        val d= showProgressDialog(this@OTPActivity,"Sending OTP...")

        binding.tvMobile.text = userModel?.phone ?: bundlePhoneNumber
        val phoneNumber = userModel?.phone ?: bundlePhoneNumber

        lifecycleScope.launch {
            withContext(Dispatchers.IO){
                mCallBack = authViewModel.signInWithPhone(phoneNumber!!, this@OTPActivity, {
                    crediential = it
                    d.dismiss()
                }, {
                    Toast.makeText(this@OTPActivity, it, Toast.LENGTH_SHORT).show()
                    d.dismiss()
                }, { id, token ->
                    verificationId = id
                    this@OTPActivity.token = token
                    showToast("code send")
                    d.dismiss()
                })
            }
        }



        binding.etC1.doAfterTextChanged {
            if (binding.etC1.text.toString().isEmpty()) {
                binding.etC1.requestFocus()
            } else {
                binding.etC2.requestFocus()
            }
        }

        binding.etC2.doAfterTextChanged {
            if (binding.etC2.text.toString().isEmpty()) {
                binding.etC1.requestFocus()
            } else {
                binding.etC3.requestFocus()
            }
        }

        binding.etC3.doAfterTextChanged {
            if (binding.etC3.text.toString().isEmpty()) {
                binding.etC2.requestFocus()
            } else {
                binding.etC4.requestFocus()
            }
        }

        binding.etC4.doAfterTextChanged {
            if (binding.etC4.text.toString().isEmpty()) {
                binding.etC3.requestFocus()
            } else {
                binding.etC5.requestFocus()
            }
        }

        binding.etC5.doAfterTextChanged {
            if (binding.etC5.text.toString().isEmpty()) {
                binding.etC4.requestFocus()
            } else {
                binding.etC6.requestFocus()
            }
        }

        binding.etC6.doAfterTextChanged {
            if (binding.etC6.text.toString().isEmpty()) {
                binding.etC5.requestFocus()
            } else {
                binding.etC6.requestFocus()
            }
        }

        binding.resendTv.setOnClickListener {
            if (::token.isInitialized){
                val dialog= showProgressDialog(this@OTPActivity,"Sending...")
                lifecycleScope.launch {
                    countDownTimer?.cancel()
                    countDownTimer = startCountdownTimer(waitingTime, binding.timerTv, this@OTPActivity,binding.resendTv)
                    val result=if (userModel!=null&&uri!=null){
                        authViewModel.resendOtp(token,mCallBack,this@OTPActivity,userModel!!.phone)
                    }else{
                        authViewModel.resendOtp(token,mCallBack,this@OTPActivity,bundlePhoneNumber!!)
                    }
                    result.whenError {
                        showToast(it.message.toString())
                    }
                    result.whenSuccess {
                        showToast(it)
                    }
                    dialog.dismiss()
                }
            }else{
                Toast.makeText(this@OTPActivity, "Token not found.", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.btnVerify.setOnClickListener {
            if (verificationId.isNotEmpty()){
                val dialog = showProgressDialog(this@OTPActivity, "Verifying...")

                val et1 = binding.etC1.text.toString().trim()
                val et2 = binding.etC2.text.toString().trim()
                val et3 = binding.etC3.text.toString().trim()
                val et4 = binding.etC4.text.toString().trim()
                val et5 = binding.etC5.text.toString().trim()
                val et6 = binding.etC6.text.toString().trim()

                if (et1.isNotEmpty() && et2.isNotEmpty() && et3.isNotEmpty() && et4.isNotEmpty() && et5.isNotEmpty() && et6.isNotEmpty()) {
                    val otp = et1 + et2 + et3 + et4 + et5 + et6
                    val creden = PhoneAuthProvider.getCredential(verificationId, otp)
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO){
                            signInWithPhoneAuthCredential(creden,dialog)
                        }
                    }
                } else {
                    dialog.dismiss()
                    Toast.makeText(this@OTPActivity, "Enter OTP", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this@OTPActivity, "verification id not found.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential,dialog:Dialog) {
        val result=authViewModel.loginWithPhoneCredential(credential)

        result.whenSuccess {

            if (uri!=null&&userModel!=null){
                lifecycleScope.launch {

                    val urlResult=storageViewModel.uploadImageToFirebaseStorage(uri!!)

                    urlResult.whenError {
                        showToast(it.message.toString())
                        dialog.dismiss()
                    }

                    urlResult.whenSuccess {it->
                        val key=auth.currentUser!!.uid
                        val currentTime = System.currentTimeMillis()
                        userModel?.timeStamp = currentTime
                        userModel?.profileUrl = it
                        userModel?.key=key

                        if (!userModel?.phone.isNullOrEmpty()){
                            lifecycleScope.launch {
                                mainViewModel.uploadAnyModel("$CONTACTS/$key", ContactModel(userModel!!.phone))
                            }
                        }

                        lifecycleScope.launch {
                            val uploadResult = mainViewModel.uploadAnyModel("Users",userModel!!)
                            uploadResult.whenSuccess {
                                startActivity(Intent(this@OTPActivity, MainActivity::class.java))
                                finishAffinity()
                            }
                            uploadResult.whenError {
                                showToast(it.message.toString())
                                dialog.dismiss()
                            }
                        }
                    }
                }
            }else{
                startActivity(Intent(this@OTPActivity, MainActivity::class.java))
                finishAffinity()
            }
            showToast(it)
        }
        result.whenError {
            showToast(it.message.toString())
            dialog.dismiss()
        }
    }
}
