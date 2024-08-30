package com.atta.chatspherapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.atta.chatspherapp.R
import com.atta.chatspherapp.databinding.ActivitySignInBinding
import com.atta.chatspherapp.models.UserModel
import com.atta.chatspherapp.ui.activities.recentchat.MainActivity
import com.atta.chatspherapp.ui.viewmodel.AuthViewModel
import com.atta.chatspherapp.ui.viewmodel.MainViewModel
import com.atta.chatspherapp.utils.Constants.CONTACTS
import com.atta.chatspherapp.utils.Constants.COUNTRYNAMECODE
import com.atta.chatspherapp.utils.Constants.GOOGLEREQUESTCODE
import com.atta.chatspherapp.utils.Constants.USERS
import com.atta.chatspherapp.utils.NewUtils.isValidEmail
import com.atta.chatspherapp.utils.NewUtils.processPhoneNumber
import com.atta.chatspherapp.utils.NewUtils.setAnimationOnView
import com.atta.chatspherapp.utils.NewUtils.setStatusBarColor
import com.atta.chatspherapp.utils.NewUtils.showErrorToast
import com.atta.chatspherapp.utils.NewUtils.showProgressDialog
import com.atta.chatspherapp.utils.NewUtils.showSoftKeyboard
import com.atta.chatspherapp.utils.NewUtils.showSuccessToast
import com.atta.chatspherapp.utils.NewUtils.showToast
import com.atta.chatspherapp.utils.NewUtils.startNewActivity
import com.atta.chatspherapp.utils.SharedPreferencesHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.shashank.sony.fancytoastlib.FancyToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class SignInActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignInBinding
    @Inject
    lateinit var mainViewModel: MainViewModel
    @Inject
    lateinit var auth:FirebaseAuth
    @Inject
    lateinit var sharedPreferencesHelper: SharedPreferencesHelper
    @Inject
    lateinit var databaseReference: DatabaseReference
    @Inject
    lateinit var authViewModel: AuthViewModel
    lateinit var mGoogleSignInClient: GoogleSignInClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarColor(R.color.green)
        overridePendingTransition(R.anim.slide_in_bottom,R.anim.slide_out_bottom)
        viewAnimation()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.web_client_id)).requestEmail().build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.registerTv.setOnClickListener {
            startNewActivity(SignUpActivity::class.java)
        }

        binding.loginBtn.setOnClickListener{
            val email=binding.emailEdt.text.toString().trim()
            val password=binding.passwordEdt.text.toString().trim()

            if (email.isEmpty()||email.isBlank()){
                binding.emailEdt.error="Enter email"
                binding.emailEdt.showSoftKeyboard()
            }else if (password.isEmpty()||password.isBlank()){
                binding.passwordEdt.error="Enter password"
                binding.passwordEdt.showSoftKeyboard()
            }else if (password.length<=6){
                binding.passwordEdt.error="Password length must be greater then 6"
                binding.passwordEdt.showSoftKeyboard()
            }else if (!email.isValidEmail()){
                binding.emailEdt.error="Enter valid email"
                binding.emailEdt.showSoftKeyboard()
            }else{
                lifecycleScope.launch {
                    val progress= showProgressDialog("Login...")
                    val loginResult=authViewModel.loginWithEmailAndPassword(email, password)
                    loginResult.whenSuccess {
                        startActivity(Intent(this@SignInActivity,MainActivity::class.java))
                        finishAffinity()
                        showSuccessToast(it)
                        progress.dismiss()
                    }
                    loginResult.whenError {
                        showErrorToast(it.message.toString())
                        progress.dismiss()
                    }
                }
            }
        }


        binding.googleSignBtn.setOnClickListener {
            authViewModel.signInWithGoogle(this@SignInActivity,GOOGLEREQUESTCODE,mGoogleSignInClient)
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            GOOGLEREQUESTCODE->{
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                try {
                    val account = task.getResult(ApiException::class.java)

                    if (account.idToken!=null){

                        lifecycleScope.launch {
                            val showProgress=showProgressDialog("Login...")

                            val result=authViewModel.signInWithGoogleFirebase(this@SignInActivity,account.idToken!!)
                            result.whenSuccess { loginSuccess->

                                val email=account.email
                                val name=  account.displayName
                                val profileUrl=  account.photoUrl.toString()
                                val authKey=auth.currentUser!!.uid

                                lifecycleScope.launch {
                                    val uploadResult=mainViewModel.uploadAnyModel(USERS,UserModel(key = authKey,profileUrl=profileUrl, fullName = name, email = email, timeStamp = System.currentTimeMillis()))

                                    uploadResult.whenSuccess {
                                        showSuccessToast(loginSuccess)
                                        showProgress.dismiss()
                                        startActivity(Intent(this@SignInActivity,MainActivity::class.java))
                                        finishAffinity()
                                    }

                                    uploadResult.whenError {
                                        showErrorToast(it.message.toString())
                                        showProgress.dismiss()
                                    }
                                }
                            }
                            result.whenError {
                                showErrorToast(it.message.toString())
                                showProgress.dismiss()
                            }
                        }
                    }
                } catch (e: ApiException) {
                    showErrorToast(e.message.toString())
                }
            }
        }
    }

    fun viewAnimation(){
        binding.card.setAnimationOnView(R.anim.slide_up,700)
        binding.loginTitle.setAnimationOnView(R.anim.slide_up,800)
        binding.emailTextInput.setAnimationOnView(R.anim.slide_up,900)
        binding.passwordTextInput.setAnimationOnView(R.anim.slide_up,1000)
        binding.loginBtn.setAnimationOnView(R.anim.slide_up,1100)
        binding.dontAccountLinear.setAnimationOnView(R.anim.slide_up,1200)
        binding.orTv.setAnimationOnView(R.anim.slide_up,1300)
        binding.googleSignBtn.setAnimationOnView(R.anim.slide_up,1400)
    }

}
