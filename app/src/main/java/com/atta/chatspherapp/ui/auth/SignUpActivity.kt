package com.atta.chatspherapp.ui.auth

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import com.atta.chatspherapp.utils.NewUtils.setAnimationOnView
import com.atta.chatspherapp.utils.NewUtils.startNewActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
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

    private lateinit var uri: Uri
    private var list = ArrayList<EditText>()
    private var requestCode=12
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
        binding.loginTv.setOnClickListener {
            finish()
        }
        animations()
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
}