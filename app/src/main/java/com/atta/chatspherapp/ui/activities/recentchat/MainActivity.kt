package com.atta.chatspherapp.ui.activities.recentchat

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import com.atta.chatspherapp.R
import com.atta.chatspherapp.databinding.ActivityMainBinding
import com.atta.chatspherapp.databinding.RecentChatItemPlaceHolderBinding
import com.atta.chatspherapp.databinding.RecentChatSampleRowBinding
import com.atta.chatspherapp.models.RecentChatModel
import com.atta.chatspherapp.models.UserModel
import com.atta.chatspherapp.service.DeleteMessagesService
import com.atta.chatspherapp.ui.activities.profile.ProfileSettingActivity
import com.atta.chatspherapp.ui.activities.room.ChatActivity
import com.atta.chatspherapp.ui.activities.searchanyuser.SearchUserForChatActivity
import com.atta.chatspherapp.ui.auth.SignInActivity
import com.atta.chatspherapp.ui.viewmodel.MainViewModel
import com.atta.chatspherapp.utils.Constants
import com.atta.chatspherapp.utils.Constants.RECENTCHAT
import com.atta.chatspherapp.utils.Constants.USERS
import com.atta.chatspherapp.utils.NewUtils.addColorRevealAnimation
import com.atta.chatspherapp.utils.NewUtils.loadImageViaLink
import com.atta.chatspherapp.utils.NewUtils.rateUS
import com.atta.chatspherapp.utils.NewUtils.setAnimationOnView
import com.atta.chatspherapp.utils.NewUtils.setData
import com.atta.chatspherapp.utils.NewUtils.setStatusBarColor
import com.atta.chatspherapp.utils.NewUtils.share
import com.atta.chatspherapp.utils.NewUtils.showToast
import com.atta.chatspherapp.utils.NewUtils.showUserImage
import com.atta.chatspherapp.utils.NewUtils.startNewActivity
import com.atta.chatspherapp.utils.NewUtils.toTimeAgo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.infideap.drawerbehavior.AdvanceDrawerLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject



@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var auth: FirebaseAuth
    lateinit var binding:ActivityMainBinding
    @Inject
    lateinit var mainViewModel: MainViewModel

    var myModel:UserModel?=null
    var sortedList = mutableListOf<RecentChatModel>()

    var duration=800L
    var animatedItemKey = mutableSetOf<String>() // Set to track animated items
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var toolbar: Toolbar


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarColor(R.color.green)
        window.statusBarColor = Color.parseColor("#299F0B")

        // navigation items

        val drawerImgProfile = findViewById<ImageView>(R.id.img_profile)
        val drawerName = findViewById<TextView>(R.id.tx_name)
        val drawerEmail = findViewById<TextView>(R.id.tx_email)

        val profileTextView = findViewById<TextView>(R.id.tx_profil)
        val shareTextView = findViewById<TextView>(R.id.tx_share)
        val rateTextView = findViewById<TextView>(R.id.tx_rate)
        val logOut = findViewById<TextView>(R.id.logOut)
        val mainRelative = findViewById<RelativeLayout>(R.id.mainRelative)

        toolbar=binding.toolbar2
        // drawer setup
        val drawer=findViewById<AdvanceDrawerLayout>(R.id.drawer)
        toggle = ActionBarDrawerToggle(this, drawer, binding.toolbar2, R.string.open, R.string.close)
        toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.white)
        binding.drawer.addDrawerListener(toggle)
        toggle.syncState()
        drawer.setViewScale(Gravity.START, .8f);
        drawer.setRadius(Gravity.START, 20.0f);
        drawer.setViewElevation(Gravity.START, 1.0f);

        drawer.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // You can update UI or handle animations here
            }

            override fun onDrawerOpened(drawerView: View) {
                val url=mainViewModel.changesProfileUrl
                val name=mainViewModel.changedName

                if (url.isNotEmpty()){
                    drawerImgProfile.loadImageViaLink(url)
                }

                if (name.isNotEmpty()){
                    drawerName.text=name
                }

                mainRelative.visibility=View.VISIBLE
                drawerImgProfile.setAnimationOnView(R.anim.bounce_anim,400)
                drawerName.setAnimationOnView(R.anim.bounce_anim,500)
                drawerEmail.setAnimationOnView(R.anim.slide_in_bottom,600)
                profileTextView.setAnimationOnView(R.anim.slide_in_bottom,700)
                shareTextView.setAnimationOnView(R.anim.slide_in_bottom,800)
                rateTextView.setAnimationOnView(R.anim.slide_in_bottom,900)
                logOut.setAnimationOnView(R.anim.slide_in_bottom,1000)
            }

            override fun onDrawerClosed(drawerView: View) {
                setStatusBarColor(R.color.green)
                mainRelative.visibility=View.GONE
            }
            override fun onDrawerStateChanged(newState: Int) {
                when (newState) {
                    DrawerLayout.STATE_DRAGGING -> {
                        setStatusBarColor(R.color.green)
                    }
                    DrawerLayout.STATE_IDLE ->{
                        setStatusBarColor(R.color.green)
                    }
                }
            }
        })


        profileTextView.setOnClickListener {
            openProfileSetting()
            true
        }

        shareTextView.setOnClickListener {
            share()
            true
        }

        rateTextView.setOnClickListener {
            rateUS()
            true
        }

        logOut.setOnClickListener {
            showLogoutDialog()
        }





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
            drawerImgProfile.loadImageViaLink(myModel?.profileUrl?:"empty")
            drawerName.text=myModel?.fullName?:"Name not found"
            drawerEmail.text=myModel?.email?:"Email not found"
        }

        binding.profileSettingImg.setOnClickListener {
            openProfileSetting()
        }

        binding.backArrow.setOnClickListener {
            onBackPressed()
        }

        lifecycleScope.launch {
            setUpRecyclerView(sortedList,true)
            mainViewModel.collectAnyModel("$RECENTCHAT/${auth.currentUser!!.uid}",RecentChatModel::class.java).collect{it->

                for (i in it){
                    val model=mainViewModel.getAnyData("$USERS/${i.key}",UserModel::class.java)
                    if (model!=null){
                        i.userModel=model
                    }
                }

                if (it.isEmpty()){
                    binding.noRecentChatMessage.visibility=View.VISIBLE
                    setUpRecyclerView(sortedList,false)
                }else{
                    binding.noRecentChatMessage.visibility=View.GONE
                }

                sortedList = it.sortedByDescending { recentModel -> recentModel.timeStamp }.toMutableList()
                setUpRecyclerView(sortedList,false)

            }
        }
    }

    fun setUpRecyclerView(sortedList: List<RecentChatModel>,isLoading:Boolean = false) {
        if (!isLoading){
            binding.recyclerView.setData(sortedList, RecentChatSampleRowBinding::inflate) { binding, recentModel, position, holder ->
                binding.profileImage.loadImageViaLink(recentModel.userModel.profileUrl)

                val maxLength = 20
                val nameLength = recentModel.userModel.fullName?.length ?: 0
                val name = recentModel.userModel.fullName
                val truncatedText = if (nameLength > maxLength) {
                    name?.substring(0, maxLength) + "..."
                } else {
                    name
                }

                binding.nameTv.text = truncatedText
                binding.recentMessage.text = recentModel.recentMessage
                binding.lastMessageTime.text = recentModel.timeStamp.toTimeAgo()

                if (recentModel.numberOfMessages != 0) {
                    binding.msgCounterTv.visibility = View.VISIBLE
                    binding.msgCounterTv.text = recentModel.numberOfMessages.toString()
                }

                holder.itemView.setOnLongClickListener {
                    addToSelectedList(recentModel, binding.mainConstraint)
                    true
                }

                binding.mainConstraint.setOnClickListener {
                    val selectedItemsList = mainViewModel.selectedItemFlow.value

                    if (myModel != null) {
                        if (selectedItemsList.isNotEmpty()) {
                            addToSelectedList(recentModel, binding.mainConstraint)
                        } else {
                            val intent = Intent(this@MainActivity, ChatActivity::class.java)
                            intent.putExtra("userModel", recentModel.userModel)
                            intent.putExtra("myModel",myModel)
                            intent.putExtra("fromRecentChat", true)
                            startActivity(intent)
                        }
                    }
                }

                binding.profileImage.setOnClickListener {
                    showUserImage(recentModel.userModel.profileUrl, recentModel.userModel.fullName ?: "Name not found")
                }

                if (!animatedItemKey.contains(recentModel.key)) {
                    binding.mainConstraint.setAnimationOnView(R.anim.slide_up, 700)
                    animatedItemKey.add(recentModel.key)
                }

            }
        }else{
            binding.recyclerView.setData(sortedList,RecentChatItemPlaceHolderBinding::inflate,isLoading){binding, recentModel, position, holder -> }
        }
    }

    @SuppressLint("SetTextI18n")
    fun addToSelectedList(recentChatModel:RecentChatModel,view:View){
        mainViewModel.addToSelectedList(recentChatModel){
            if (it){
                addColorRevealAnimation(view,500,ContextCompat.getColor(this@MainActivity, R.color.ver_light_green))
            }else{
                view.setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.white))
            }
            toolBarSettings()
        }
    }


    var animationApplied=true
    @SuppressLint("SetTextI18n")
    fun toolBarSettings(){

        val list=mainViewModel.selectedItemFlow.value

        if (list.isNotEmpty()){

            showDrawerIcon(false)
            val size=list.size
            binding.toolBarTitle.text="${size} selected"
            binding.toolBarTitle.setAnimationOnView(R.anim.scale,800)
            binding.profileSettingImg.visibility=View.GONE
            binding.deleteImg.visibility=View.VISIBLE
            binding.backArrow.visibility=View.VISIBLE

            if (animationApplied){
                binding.deleteImg.setAnimationOnView(R.anim.slide_in_bottom,800)
                binding.backArrow.setAnimationOnView(R.anim.slide_in_bottom,800)
                animationApplied=false
            }

        }else{

            showDrawerIcon(true)
            binding.toolBarTitle.text="Recent chat"
            binding.profileSettingImg.visibility=View.VISIBLE
            binding.deleteImg.visibility=View.GONE
            binding.backArrow.visibility=View.GONE

            if (!animationApplied){
                binding.backArrow.setAnimationOnView(R.anim.slide_out_bottom,800)
                binding.deleteImg.setAnimationOnView(R.anim.slide_out_bottom,800)
                binding.profileSettingImg.setAnimationOnView(R.anim.slide_in_bottom,800)
                binding.toolBarTitle.setAnimationOnView(R.anim.slide_in_bottom,800)
                animationApplied=true
            }

            return

        }

        binding.deleteImg.setOnClickListener {
            val alert=AlertDialog.Builder(this@MainActivity).setView(com.atta.chatspherapp.R.layout.delete_chat_dialog).show()
            val cancelBtn=alert.findViewById<Button>(com.atta.chatspherapp.R.id.cancelBtn)
            val deleteBtn=alert.findViewById<Button>(com.atta.chatspherapp.R.id.deleteBtn)
            val dialogTitle=alert.findViewById<TextView>(com.atta.chatspherapp.R.id.dialogTitle)

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


    override fun onBackPressed() {
        val list=mainViewModel.selectedItemFlow.value
        if (list.isNotEmpty()){
            mainViewModel.clearSelectedItemsList()
            toolBarSettings()
            setUpRecyclerView(sortedList)
            showDrawerIcon(true)
        }else if (binding.drawer.isDrawerOpen(GravityCompat.START)) {
            binding.drawer.closeDrawer(GravityCompat.START)
        } else{
            super.onBackPressed()
        }
    }


    fun openProfileSetting(){
        if (myModel!=null){
            val intent=Intent(this@MainActivity, ProfileSettingActivity::class.java)
            intent.putExtra("myModel",myModel)
            startActivity(intent)
        }else{
            showToast("Something wrong or check internet connect.")
        }
    }

    private fun showDrawerIcon(show: Boolean) {
        if (show) {
            toggle.isDrawerIndicatorEnabled = true
            toolbar.navigationIcon = toggle.drawerArrowDrawable
        } else {
            toggle.isDrawerIndicatorEnabled = false
            toolbar.navigationIcon = null
        }
    }


    private fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Do you want to logout?")

        builder.setPositiveButton("Logout") { dialog: DialogInterface, _: Int ->
            auth.signOut()
            startNewActivity(SignInActivity::class.java,true)
            dialog.dismiss()
        }

        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    override fun onResume() {
        super.onResume()
        val url = mainViewModel.changesProfileUrl
        val name = mainViewModel.changedName
        val myKey = auth.currentUser!!.uid
        if (url.isNotEmpty()||name.isNotEmpty()){
            val updatedList = sortedList.map { item ->
                if (item.key == myKey) {
                    item.copy(userModel = item.userModel.copy(profileUrl = url.ifEmpty { item.userModel.profileUrl}, fullName = name.ifEmpty { item.userModel.fullName }))
                } else {
                    item
                }
            }
            setUpRecyclerView(updatedList)

            if (url.isNotEmpty()){
                myModel=myModel?.copy(profileUrl = url)
            }

            if (name.isNotEmpty()){
                myModel=myModel?.copy(fullName = name)
            }

        }
    }

}