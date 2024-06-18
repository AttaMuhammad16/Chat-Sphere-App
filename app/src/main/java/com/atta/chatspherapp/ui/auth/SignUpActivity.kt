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
        binding=ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferencesHelper= SharedPreferencesHelper(this@SignUpActivity)

        setStatusBarColor(R.color.black)
        Glide.with(this@SignUpActivity).load(R.drawable.bg).into(binding.imageView)
        binding.selectUserImage.setOnClickListener {
            pickImageFromGallery(requestCode,this@SignUpActivity)
        }

        binding.loginTv.setOnClickListener {
            startActivity(Intent(this@SignUpActivity, SignInActivity::class.java))
            finish()
        }

        requestStoragePermission()

        binding.registerBtn.setOnClickListener {
            val name=binding.nameEdt.text.toString().trim()

            binding.countryCodePicker.registerCarrierNumberEditText(binding.numberEdt)
            val dialog= showProgressDialog(this,"Register...")
            val countryCode=binding.countryCodePicker.selectedCountryCode
            val number=binding.numberEdt.text.toString()
            val formattedNumber=processPhoneNumber(number)
            val fullNumber="+${countryCode+formattedNumber}"
            val selectedCountryNameCode=binding.countryCodePicker.selectedCountryNameCode

            list.add(binding.nameEdt)
            list.add(binding.numberEdt)

            if (!::uri.isInitialized){
                showToast("Select profile image", Toast.LENGTH_LONG)
                dialog.dismiss()
            }else if (!checkEditTexts(this@SignUpActivity,list)){
                dialog.dismiss()
            }else if (fullNumber.length<=5){
                showToast("Enter valid PhoneNumber", Toast.LENGTH_SHORT)
                binding.numberEdt.error = "Enter valid PhoneNumber"
                dialog.dismiss()
            } else{

                lifecycleScope.launch {

                    sharedPreferencesHelper.putString(COUNTRYNAMECODE,selectedCountryNameCode)
                    val key=databaseReference.push().key.toString()
                    val result=mainViewModel.uploadAnyModel("$CONTACTS/$key",ContactModel(fullNumber))

                    result.whenSuccess {
                        val userModel= UserModel("",name,fullNumber,"","",0,"")
                        val intent=Intent(this@SignUpActivity, OTPActivity::class.java)
                        intent.putExtra("userModel",userModel)
                        intent.putExtra("uri",uri.toString())
                        startActivity(intent)
                        finish()
                        dialog.dismiss()
                    }

                    result.whenError {
                        showToast(it.message.toString())
                        dialog.dismiss()
                    }

               }
            }
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode== Activity.RESULT_OK&&requestCode==this.requestCode){
            uri=data?.data?: Uri.parse("")
            binding.selectedImg.setImageURI(uri)
            val takeFlags = data?.flags?.and((Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
            if (takeFlags != null) {
                contentResolver.takePersistableUriPermission(uri, takeFlags)
            }
        }
    }

    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1321)
        }
    }

}