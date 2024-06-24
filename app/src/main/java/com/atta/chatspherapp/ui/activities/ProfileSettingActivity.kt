package com.atta.chatspherapp.ui.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.atta.chatspherapp.R
import com.atta.chatspherapp.databinding.ActivityProfileSettingBinding
import com.atta.chatspherapp.models.UserModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ProfileSettingActivity : AppCompatActivity() {
    lateinit var binding:ActivityProfileSettingBinding
    var myModel:UserModel?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityProfileSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        myModel=intent.getParcelableExtra("myModel")

    }
}