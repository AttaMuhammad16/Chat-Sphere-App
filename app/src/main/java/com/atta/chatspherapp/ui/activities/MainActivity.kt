package com.atta.chatspherapp.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.atta.chatspherapp.R
import com.atta.chatspherapp.databinding.ActivityMainBinding
import com.atta.chatspherapp.utils.NewUtils.setStatusBarColor
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject



@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var auth: FirebaseAuth
    lateinit var binding:ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarColor(R.color.green)

        binding.chatCard.setOnClickListener{
            startActivity(Intent(this@MainActivity,SearchUserForChatActivity::class.java))
        }

    }
}