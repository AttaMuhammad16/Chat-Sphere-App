package com.atta.chatspherapp.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.atta.chatspherapp.R
import com.atta.chatspherapp.databinding.ActivitySignInBinding
import com.atta.chatspherapp.ui.activities.recentchat.MainActivity
import com.atta.chatspherapp.ui.viewmodel.MainViewModel
import com.atta.chatspherapp.utils.Constants.CONTACTS
import com.atta.chatspherapp.utils.Constants.COUNTRYNAMECODE
import com.atta.chatspherapp.utils.NewUtils.processPhoneNumber
import com.atta.chatspherapp.utils.NewUtils.setStatusBarColor
import com.atta.chatspherapp.utils.NewUtils.showProgressDialog
import com.atta.chatspherapp.utils.NewUtils.showToast
import com.atta.chatspherapp.utils.SharedPreferencesHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarColor(R.color.green)
        if (auth.currentUser!=null){
            startActivity(Intent(this@SignInActivity, MainActivity::class.java))
            finish()
        }




    }

}
