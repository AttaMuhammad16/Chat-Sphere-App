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
        setStatusBarColor(R.color.black)
        sharedPreferencesHelper= SharedPreferencesHelper(this)

        if (auth.currentUser!=null){
            startActivity(Intent(this@SignInActivity, MainActivity::class.java))
            finish()
        }


        binding.loginBtn.setOnClickListener {

            val number=binding.numberEdt.text.toString()
            binding.countryCodePicker.registerCarrierNumberEditText(binding.numberEdt)
            val countryCode=binding.countryCodePicker.selectedCountryCode
            val formattedNumber= processPhoneNumber(number)
            val fullNumber="+${countryCode+formattedNumber}"
            val selectedCountryNameCode=binding.countryCodePicker.selectedCountryNameCode

            if (number.isEmpty()){
                showToast("Enter phoneNumber", Toast.LENGTH_SHORT)
                binding.numberEdt.error = "Enter phoneNumber"
            } else if (fullNumber.length<=5){
                showToast("Enter valid PhoneNumber", Toast.LENGTH_SHORT)
                binding.numberEdt.error = "Enter valid PhoneNumber"
            } else{

                val dialog= showProgressDialog(this@SignInActivity,"SignIn...")

                lifecycleScope.launch {
                    val result=mainViewModel.checkPhoneNumberExists(CONTACTS,fullNumber)

                    result.whenSuccess {
                        sharedPreferencesHelper.getString(COUNTRYNAMECODE, selectedCountryNameCode)
                        val intent = Intent(this@SignInActivity, OTPActivity::class.java)
                        intent.putExtra("phoneNumber", fullNumber)
                        startActivity(intent)
                        dialog.dismiss()
                    }

                    result.whenError {
                        showToast("Please register your phoneNumber.It does not exist.")
                        dialog.dismiss()
                    }

                }
            }
        }

        binding.registerTv.setOnClickListener {
            startActivity(Intent(this@SignInActivity, SignUpActivity::class.java))
        }

    }



}
