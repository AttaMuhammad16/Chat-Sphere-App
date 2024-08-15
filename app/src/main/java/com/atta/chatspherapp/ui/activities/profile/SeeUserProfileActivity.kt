package com.atta.chatspherapp.ui.activities.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.atta.chatspherapp.R
import com.atta.chatspherapp.databinding.ActivitySeeUserProfileBinding
import com.atta.chatspherapp.models.UserModel
import com.atta.chatspherapp.utils.NewUtils.formatDateFromMillis
import com.atta.chatspherapp.utils.NewUtils.loadImageFromResource
import com.atta.chatspherapp.utils.NewUtils.loadImageViaLink
import com.atta.chatspherapp.utils.NewUtils.setAnimationOnView
import com.atta.chatspherapp.utils.NewUtils.setStatusBarColor
import com.atta.chatspherapp.utils.NewUtils.showToast


class SeeUserProfileActivity : AppCompatActivity() {
    lateinit var binding:ActivitySeeUserProfileBinding
    var userBundle:UserModel?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySeeUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarColor(R.color.green)
        userBundle=intent.getParcelableExtra("userModel")
        overridePendingTransition(R.anim.slide_in_bottom,R.anim.slide_out_bottom)

        binding.imgConstraint.setAnimationOnView(R.anim.scale,1000)
        binding.nameLinear.setAnimationOnView(R.anim.bounce_anim,1200)
        binding.aboutLinear.setAnimationOnView(R.anim.bounce_anim,1200)
        binding.joiningLinear.setAnimationOnView(R.anim.bounce_anim,1200)



        binding.backImg.setOnClickListener {
            finish()
        }

        userBundle?.apply {
            binding.profileImg.loadImageViaLink(profileUrl)
            binding.userNameTv.text=fullName
            binding.statusTv.text=if (status.isNotEmpty()){status}else{"Hey there! i am using chat sphere"}
            binding.joiningDate.text= formatDateFromMillis(timeStamp,"dd MMM yyyy hh:mm a")
        }

    }
}