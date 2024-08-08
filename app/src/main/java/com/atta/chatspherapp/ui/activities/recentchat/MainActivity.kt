package com.atta.chatspherapp.ui.activities.recentchat

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.atta.chatspherapp.R
import com.atta.chatspherapp.databinding.ActivityMainBinding
import com.atta.chatspherapp.databinding.RecentChatSampleRowBinding
import com.atta.chatspherapp.models.RecentChatModel
import com.atta.chatspherapp.models.UserModel
import com.atta.chatspherapp.service.DeleteMessagesService
import com.atta.chatspherapp.ui.activities.searchanyuser.SearchUserForChatActivity
import com.atta.chatspherapp.ui.activities.profile.ProfileSettingActivity
import com.atta.chatspherapp.ui.activities.room.ChatActivity
import com.atta.chatspherapp.ui.viewmodel.MainViewModel
import com.atta.chatspherapp.utils.Constants
import com.atta.chatspherapp.utils.Constants.RECENTCHAT
import com.atta.chatspherapp.utils.Constants.USERS
import com.atta.chatspherapp.utils.NewUtils.loadImageViaLink
import com.atta.chatspherapp.utils.NewUtils.setData
import com.atta.chatspherapp.utils.NewUtils.setStatusBarColor
import com.atta.chatspherapp.utils.NewUtils.showProgressDialog
import com.atta.chatspherapp.utils.NewUtils.showToast
import com.atta.chatspherapp.utils.NewUtils.showUserImage
import com.atta.chatspherapp.utils.NewUtils.toTimeAgo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject


// for audio calling will add agora or zegocloud

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var auth: FirebaseAuth
    lateinit var binding:ActivityMainBinding
    @Inject
    lateinit var mainViewModel: MainViewModel

    var myModel:UserModel?=null
    var toggle=true
    var sortedList = mutableListOf<RecentChatModel>()


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarColor(R.color.green)

//       generate FCM token
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (it.isSuccessful) {
                lifecycleScope.launch {
                    val map=HashMap<String,Any>()
                    map[Constants.FCMTOENNODE] = it.result
                    mainViewModel.uploadMap(USERS + "/" + auth.currentUser!!.uid,map)
                }
            }
        }


        binding.chatCard.setOnClickListener{
            startActivity(Intent(this@MainActivity, SearchUserForChatActivity::class.java))
        }

        lifecycleScope.launch {
            myModel=mainViewModel.getAnyData("$USERS/${auth.currentUser!!.uid}",UserModel::class.java)
        }

        binding.profileSettingImg.setOnClickListener {
            if (myModel!=null){
                val intent=Intent(this@MainActivity, ProfileSettingActivity::class.java)
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
                    delay(2000)
                    progress.dismiss()
                }
                sortedList = it.sortedByDescending { recentModel -> recentModel.timeStamp }.toMutableList()
                setUpRecyclerView(sortedList)

            }
        }

    }

    fun setUpRecyclerView(sortedList:List<RecentChatModel>){
        binding.recyclerView.setData(sortedList, RecentChatSampleRowBinding::inflate) { binding, recentModel, position,holder->

            binding.profileImage.loadImageViaLink(recentModel.userModel.profileUrl)

            binding.nameTv.text=recentModel.userModel.fullName
            binding.recentMessage.text=recentModel.recentMessage
            binding.lastMessageTime.text=recentModel.timeStamp.toTimeAgo()

            if (recentModel.numberOfMessages != 0) {
                binding.msgCounterTv.visibility=View.VISIBLE
                binding.msgCounterTv.text=recentModel.numberOfMessages.toString()
            }

            holder.itemView.setOnLongClickListener {
                addToSelectedList(recentModel,binding.mainConstraint)
                true
            }

            binding.mainConstraint.setOnClickListener {
                val selectedItemsList=mainViewModel.selectedItemFlow.value
                if (myModel!=null){
                    if (selectedItemsList.isNotEmpty()){
                        addToSelectedList(recentModel,binding.mainConstraint)
                    }else{
                        val intent=Intent(this@MainActivity, ChatActivity::class.java)
                        intent.putExtra("userModel",recentModel.userModel)
                        intent.putExtra("myModel",myModel)
                        intent.putExtra("fromRecentChat",true)
                        startActivity(intent)
                    }
                }
            }

            binding.profileImage.setOnClickListener{
                showUserImage(recentModel.userModel.profileUrl,recentModel.userModel.phone)
            }

            if (toggle){
                val animation = AnimationUtils.loadAnimation(this@MainActivity, android.R.anim.slide_in_left)
                holder.itemView.startAnimation(animation)
                toggle=false
            }

        }
    }


    @SuppressLint("SetTextI18n")
    fun addToSelectedList(recentChatModel:RecentChatModel,view:View){
        mainViewModel.addToSelectedList(recentChatModel){
            if (it){
                view.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.light_green))
            }else{
                view.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.white))
            }
            toolBarSettings()
        }
    }



    @SuppressLint("SetTextI18n")
    fun toolBarSettings(){

        val list=mainViewModel.selectedItemFlow.value

        if (list.isNotEmpty()){
            binding.toolBarTitle.text="${list.size} selected"
            binding.profileSettingImg.visibility=View.GONE
            binding.deleteImg.visibility=View.VISIBLE
        }else{
            binding.toolBarTitle.text=ContextCompat.getString(this@MainActivity,R.string.chat_sphere_for_random_chat)
            binding.profileSettingImg.visibility=View.VISIBLE
            binding.deleteImg.visibility=View.GONE
            return
        }

        binding.deleteImg.setOnClickListener {
            val alert=AlertDialog.Builder(this@MainActivity).setView(R.layout.delete_chat_dialog).show()
            val cancelBtn=alert.findViewById<Button>(R.id.cancelBtn)
            val deleteBtn=alert.findViewById<Button>(R.id.deleteBtn)
            val dialogTitle=alert.findViewById<TextView>(R.id.dialogTitle)

            if (list.size>1) {
                dialogTitle.text="Delete ${list.size} chats?"
            }else{
                dialogTitle.text="Delete ${list.size} chat?"
            }

            cancelBtn.setOnClickListener{
                alert.dismiss()
            }

            deleteBtn.setOnClickListener {
                alert.dismiss()

                val intent = Intent(this@MainActivity,DeleteMessagesService::class.java)
                val arraylist=ArrayList(list)
                intent.putExtra("selectedMessages",arraylist)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                }else{
                    startService(intent)
                }
                for ((i,model) in list.withIndex()){
                    sortedList.remove(model)
                }
                setUpRecyclerView(sortedList)
                mainViewModel.clearSelectedItemsList()
                toolBarSettings()
            }

            alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }


}