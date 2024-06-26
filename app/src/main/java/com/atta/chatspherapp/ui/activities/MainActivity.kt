package com.atta.chatspherapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.atta.chatspherapp.R
import com.atta.chatspherapp.databinding.ActivityMainBinding
import com.atta.chatspherapp.databinding.RecentChatSampleRowBinding
import com.atta.chatspherapp.databinding.UserSampleRowBinding
import com.atta.chatspherapp.models.RecentChatModel
import com.atta.chatspherapp.models.UserModel
import com.atta.chatspherapp.ui.activities.room.ChatActivity
import com.atta.chatspherapp.ui.viewmodel.MainViewModel
import com.atta.chatspherapp.utils.Constants.RECENTCHAT
import com.atta.chatspherapp.utils.Constants.USERS
import com.atta.chatspherapp.utils.MyExtensions.logT
import com.atta.chatspherapp.utils.NewUtils.millisToTime12hFormat
import com.atta.chatspherapp.utils.NewUtils.setData
import com.atta.chatspherapp.utils.NewUtils.setStatusBarColor
import com.atta.chatspherapp.utils.NewUtils.showProgressDialog
import com.atta.chatspherapp.utils.NewUtils.showToast
import com.atta.chatspherapp.utils.NewUtils.toFormattedDate
import com.atta.chatspherapp.utils.NewUtils.toTimeAgo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject



@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var auth: FirebaseAuth
    lateinit var binding:ActivityMainBinding
    @Inject
    lateinit var mainViewModel: MainViewModel
    var myModel=UserModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarColor(R.color.green)

        binding.chatCard.setOnClickListener{
            startActivity(Intent(this@MainActivity,SearchUserForChatActivity::class.java))
        }

        lifecycleScope.launch {
            myModel=mainViewModel.getAnyData("$USERS/${auth.currentUser!!.uid}",UserModel::class.java)!!
        }

        binding.profileSettingImg.setOnClickListener {
            if (myModel.fullName.isNotEmpty()){
                val intent=Intent(this@MainActivity,ProfileSettingActivity::class.java)
                intent.putExtra("myModel",myModel)
                startActivity(intent)
            }else{
                showToast("Something wrong or check internet connect.")
            }
        }

        lifecycleScope.launch {
            val progress= showProgressDialog(this@MainActivity,"Loading...")

            mainViewModel.collectAnyModel("$RECENTCHAT/${auth.currentUser!!.uid}",RecentChatModel::class.java).collect{it->

                for (i in it){
                    val model=mainViewModel.getAnyData("$USERS/${i.key}",UserModel::class.java)
                    if (model!=null){
                        i.userModel=model
                    }
                }

                if (it.isNotEmpty()){
                    progress.dismiss()
                }else{
                    delay(4000)
                    progress.dismiss()
                }

                val sortedList = it.sortedByDescending { recentModel -> recentModel.timeStamp }

                binding.recyclerView.setData(sortedList, RecentChatSampleRowBinding::inflate) { binding, recentModel, position ->

                  Picasso.get().load(recentModel.userModel.profileUrl).placeholder(R.drawable.person).into(binding.profileImage)

                    binding.nameTv.text=recentModel.userModel.fullName
                    binding.recentMessage.text=recentModel.recentMessage
                    binding.lastMessageTime.text=recentModel.timeStamp.toTimeAgo()

                    if (recentModel.numberOfMessages != 0) {
                        binding.msgCounterTv.visibility=View.VISIBLE
                        binding.msgCounterTv.text=recentModel.numberOfMessages.toString()
                    }

                    binding.mainConstraint.setOnClickListener {
                        if (myModel.fullName.isNotEmpty()){
                            val intent=Intent(this@MainActivity, ChatActivity::class.java)
                            intent.putExtra("userModel",recentModel.userModel)
                            intent.putExtra("myModel",myModel)
                            intent.putExtra("fromRecentChat",true)
                            startActivity(intent)
                        }
                    }

                }
            }
        }


    }
}