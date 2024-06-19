package com.atta.chatspherapp.ui.activities

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.atta.chatspherapp.R
import com.atta.chatspherapp.databinding.ActivitySearchUserForChatBinding
import com.atta.chatspherapp.databinding.UserSampleRowBinding
import com.atta.chatspherapp.models.UserModel
import com.atta.chatspherapp.ui.activities.room.ChatActivity
import com.atta.chatspherapp.ui.viewmodel.MainViewModel
import com.atta.chatspherapp.utils.Constants
import com.atta.chatspherapp.utils.Constants.USERS
import com.atta.chatspherapp.utils.NewUtils.hideKeyboard
import com.atta.chatspherapp.utils.NewUtils.hideWithRevealAnimation
import com.atta.chatspherapp.utils.NewUtils.onTextChange
import com.atta.chatspherapp.utils.NewUtils.setData
import com.atta.chatspherapp.utils.NewUtils.setStatusBarColor
import com.atta.chatspherapp.utils.NewUtils.showKeyBoard
import com.atta.chatspherapp.utils.NewUtils.showWithRevealAnimation
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class SearchUserForChatActivity : AppCompatActivity() {

    lateinit var binding:ActivitySearchUserForChatBinding
    @Inject
    lateinit var auth: FirebaseAuth
    @Inject
    lateinit var mainViewModel: MainViewModel
    var list = listOf<UserModel>()
    private val filteredList = mutableListOf<UserModel>()
    var myModel=UserModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivitySearchUserForChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setStatusBarColor(R.color.green)


        lifecycleScope.launch {
            myModel=mainViewModel.getAnyData("${USERS}/${auth.currentUser!!.uid}",UserModel::class.java)!!
        }


        binding.searchImg.setOnClickListener {
            hideWithRevealAnimation(binding.toolbar,binding.searchLinear)
            binding.searchEdt.requestFocus()
            showKeyBoard(this, binding.searchEdt)
        }

        binding.backImg.setOnClickListener {
            finish()
        }

        binding.searchBackImg.setOnClickListener {
            showWithRevealAnimation(binding.toolbar, binding.searchLinear)
            binding.searchEdt.clearFocus()
            binding.searchEdt.setText("")
            hideKeyboard(this, binding.searchEdt)
        }

        lifecycleScope.launch {
            mainViewModel.collectAnyModel(USERS,UserModel::class.java).collect{
                list=it
                binding.totalUsers.text=if (it.size==1){"${it.size} User"}else{"${it.size} Users"}
                setUpRecyclerView(it)
            }
        }

        binding.searchEdt.onTextChange {
            filterList(it)
        }

    }

    private fun filterList(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(list)
        } else {
            list.filterTo(filteredList) { it.fullName.contains(query, ignoreCase = true) }
        }
        setUpRecyclerView(filteredList)
    }


    fun zoomImage(userModel: UserModel) {
        val alert = AlertDialog.Builder(this).setView(R.layout.pop_up_image_dialog).show()

//        val width = resources.displayMetrics.widthPixels * 75 / 100
        val window = alert.window
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(window?.attributes)
//        layoutParams.width = width

        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.attributes = layoutParams

        val userNumberTv = alert.findViewById<TextView>(R.id.numberTv)
        val userImage = alert.findViewById<ImageView>(R.id.imageView)
        Picasso.get().load(userModel.profileUrl).placeholder(R.drawable.person).into(userImage)
        userNumberTv?.text = userModel.phone
    }

    fun setUpRecyclerView(list:List<UserModel>){

        val sortedList = list.sortedByDescending { it.key == auth.currentUser!!.uid}

        binding.recyclerView.setData(items = sortedList, bindingInflater = UserSampleRowBinding::inflate, bindHolder = {binding, item, position ->

            Picasso.get().load(item.profileUrl).placeholder(R.drawable.person).into(binding.userImage)
            binding.userNameTv.text=if (auth.currentUser!!.uid==item.key){"${item.fullName} (You)"}else{item.fullName}
            binding.statusTv.text=if (item.status.isEmpty()){"Hey there i am using Chat Sphere"}else{item.status}
            binding.userImage.setOnClickListener{
                zoomImage(item)
            }

            binding.main.setOnClickListener {
                if (myModel.fullName.isNotEmpty()){
                    val intent=Intent(this@SearchUserForChatActivity, ChatActivity::class.java)
                    intent.putExtra("userModel",item)
                    intent.putExtra("myModel",myModel)
                    startActivity(intent)
                }
            }
        })
    }

}