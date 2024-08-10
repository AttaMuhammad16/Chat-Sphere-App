package com.atta.chatspherapp.ui.activities.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.atta.chatspherapp.R
import com.atta.chatspherapp.databinding.ActivityProfileSettingBinding
import com.atta.chatspherapp.models.UserModel
import com.atta.chatspherapp.ui.viewmodel.MainViewModel
import com.atta.chatspherapp.ui.viewmodel.StorageViewModel
import com.atta.chatspherapp.utils.NewUtils.loadImageViaLink
import com.atta.chatspherapp.utils.NewUtils.pickImageFromGallery
import com.atta.chatspherapp.utils.NewUtils.setStatusBarColor
import com.atta.chatspherapp.utils.NewUtils.showProgressDialog
import com.atta.chatspherapp.utils.NewUtils.showSoftKeyboard
import com.atta.chatspherapp.utils.NewUtils.showToast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class ProfileSettingActivity : AppCompatActivity() {
    lateinit var binding:ActivityProfileSettingBinding
    var myModel:UserModel?=null
    lateinit var uri:Uri
    @Inject
    lateinit var storageViewModel: StorageViewModel
    @Inject
    lateinit var mainViewModel: MainViewModel
    @Inject
    lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityProfileSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarColor(R.color.green)
        myModel=intent.getParcelableExtra("myModel")
        overridePendingTransition(R.anim.slide_in_bottom,R.anim.slide_out_bottom)

        myModel?.apply {
            val s=if (status.isNotEmpty()){status}else{"Available"}
            binding.profileImg.loadImageViaLink(profileUrl)
            binding.userNameTv.text=fullName
            binding.statusTv.text=s
            binding.phoneNumberTv.text=fullName
            status=s
        }

        binding.backImg.setOnClickListener {
            finish()
        }

        binding.cameraImg.setOnClickListener {
            pickImageFromGallery(12,this@ProfileSettingActivity)
        }

        binding.nameLinear.setOnClickListener {
            showBottomSheet(myModel?.fullName?:"Name not found",true)
        }

        binding.aboutLinear.setOnClickListener {
           showBottomSheet(myModel!!.status,false)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 12 &&resultCode==Activity.RESULT_OK) {
            uri=data!!.data!!
            binding.profileImg.setImageURI(uri)
            lifecycleScope.launch {
                val progress= showProgressDialog("Changing...")
                storageViewModel.deleteDocumentToFirebaseStorage(myModel!!.profileUrl)
                val result=storageViewModel.uploadImageToFirebaseStorage(uri.toString())
                result.whenSuccess {
                    lifecycleScope.launch {
                        val map=HashMap<String,Any>()
                        map["profileUrl"]=it
                        val updateResult=mainViewModel.uploadMap("Users"+"/"+auth.currentUser!!.uid,map)
                        updateResult.whenError {
                            showToast(it.message.toString())
                            progress.dismiss()
                        }
                        updateResult.whenSuccess {
                            progress.dismiss()
                        }
                    }
                }
                result.whenError {
                    showToast(it.message.toString())
                    progress.dismiss()
                }
            }
        }
    }


    fun showBottomSheet(data: String,from:Boolean) {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_with_edit_text, null)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val edt=view.findViewById<EditText>(R.id.edt)
        edt.setText(data)
        edt.requestFocus()
        edt.showSoftKeyboard()

        val cancelBtn=view.findViewById<Button>(R.id.cancelBtn)
        val saveBtn=view.findViewById<Button>(R.id.saveBtn)

        saveBtn.setOnClickListener {
            val text=edt.text.toString().trim()
            if (from) {
                if (text.isNotEmpty()){
                    lifecycleScope.launch {
                        val map=HashMap<String,Any>()
                        map["fullName"]=text
                        val updateResult=mainViewModel.uploadMap("Users"+"/"+auth.currentUser!!.uid,map)
                        updateResult.whenError {
                            showToast(it.message.toString())
                        }
                        updateResult.whenSuccess {
                            bottomSheetDialog.dismiss()
                        }
                    }
                }else{
                    showToast("Enter your name.")
                }
            }else{
                if (text.isNotEmpty()){
                    lifecycleScope.launch {
                        binding.statusTv.text=text
                        val map=HashMap<String,Any>()
                        map["status"]=text
                        val updateResult=mainViewModel.uploadMap("Users"+"/"+auth.currentUser!!.uid,map)
                        updateResult.whenError {
                            showToast(it.message.toString())
                        }
                        updateResult.whenSuccess {
                            bottomSheetDialog.dismiss()
                        }
                    }
                }else{
                    showToast("Enter your status.")
                }
            }
        }
        cancelBtn.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }


}