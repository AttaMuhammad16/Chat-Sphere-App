package com.atta.chatspherapp.ui.activities.room

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DownloadManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atta.chatspherapp.R
import com.atta.chatspherapp.databinding.ActivityChatBinding
import com.atta.chatspherapp.managers.MyNotificationManager
import com.atta.chatspherapp.models.MessageModel
import com.atta.chatspherapp.models.ReactionModel
import com.atta.chatspherapp.models.UserModel
import com.atta.chatspherapp.receiver.DownloadReceiver
import com.atta.chatspherapp.service.UploadDocumentService
import com.atta.chatspherapp.service.UploadImageService
import com.atta.chatspherapp.service.UploadVideoService
import com.atta.chatspherapp.ui.viewmodel.MainViewModel
import com.atta.chatspherapp.ui.viewmodel.StorageViewModel
import com.atta.chatspherapp.utils.MyExtensions.logT
import com.atta.chatspherapp.utils.NewUtils
import com.atta.chatspherapp.utils.NewUtils.animateViewFromBottom
import com.atta.chatspherapp.utils.NewUtils.animateViewHideToBottom
import com.atta.chatspherapp.utils.NewUtils.getFileName
import com.atta.chatspherapp.utils.NewUtils.getUriOfTheFile
import com.atta.chatspherapp.utils.NewUtils.gotoEditActivity
import com.atta.chatspherapp.utils.NewUtils.loadThumbnail
import com.atta.chatspherapp.utils.NewUtils.onTextChange
import com.atta.chatspherapp.utils.NewUtils.pickDocument
import com.atta.chatspherapp.utils.NewUtils.pickImageFromGallery
import com.atta.chatspherapp.utils.NewUtils.pickVideo
import com.atta.chatspherapp.utils.NewUtils.setStatusBarColor
import com.atta.chatspherapp.utils.NewUtils.showSoftKeyboard
import com.atta.chatspherapp.utils.NewUtils.showToast
import com.atta.chatspherapp.utils.NewUtils.uploadAudioToFirebase
import com.atta.chatspherapp.utils.NewUtils.zoomIn
import com.atta.chatspherapp.utils.SharedPreferencesHelper
import com.bumptech.glide.Glide
import com.google.firebase.database.DatabaseReference
import com.google.firebase.storage.StorageReference
import com.atta.chatspherapp.adapters.ChatAdapter
import com.atta.chatspherapp.adapters.ChatAdapter.Companion.setAdapter
import com.atta.chatspherapp.managers.NotificationManager.Companion.clearNotifications
import com.atta.chatspherapp.models.RecentChatModel
import com.atta.chatspherapp.ui.activities.profile.SeeUserProfileActivity
import com.atta.chatspherapp.utils.Constants.ACTIVITYSTATEOFTHEUSER
import com.atta.chatspherapp.utils.Constants.CHATTINGWITH
import com.atta.chatspherapp.utils.Constants.DOCUMENT
import com.atta.chatspherapp.utils.Constants.IMAGE
import com.atta.chatspherapp.utils.Constants.RECENTCHAT
import com.atta.chatspherapp.utils.Constants.TEXT
import com.atta.chatspherapp.utils.Constants.USERS
import com.atta.chatspherapp.utils.Constants.VIDEO
import com.atta.chatspherapp.utils.Constants.VOICE
import com.atta.chatspherapp.utils.MyExtensions.shrink
import com.atta.chatspherapp.utils.NewUtils.getAccessToken
import com.atta.chatspherapp.utils.NewUtils.getSortedKeys
import com.atta.chatspherapp.utils.NewUtils.loadImageViaLink
import com.atta.chatspherapp.utils.SendNotification
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import javax.inject.Inject


@AndroidEntryPoint
class ChatActivity : AppCompatActivity() {

    lateinit var binding: ActivityChatBinding

    @Inject
    lateinit var mainViewModel: MainViewModel

    @Inject
    lateinit var databaseReference: DatabaseReference
    var bol = false
    var mediaRecorder: MediaRecorder? = null
    private var filePath: String? = null
    val handler = Handler(Looper.getMainLooper())

    var userModel: UserModel? = null
    var userUid = ""
    lateinit var adapter: ChatAdapter
    lateinit var mediaPlayer: MediaPlayer

    @Inject
    lateinit var storageViewModel: StorageViewModel

    @Inject
    lateinit var storageReference: StorageReference

    lateinit var chatUploadPath: String

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this@ChatActivity, "permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val pickImageRequestCode = 12
    val cameraImageRequestCode = 13
    val cropImageRequestCode = 16

    val pickVideoRequestCode = 14
    val pickDocumentRequestCode = 15

    lateinit var list:ArrayList<MessageModel>
    lateinit var preferencesHelper: SharedPreferencesHelper
    lateinit var referenceMessageModel: MessageModel
    var messageModel = MessageModel()
    @Inject
    lateinit var auth: FirebaseAuth
    var myModel=UserModel()
    var fromRecentChat=false
    var userActivityState:Boolean=false

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("SuspiciousIndentation", "UnspecifiedRegisterReceiverFlag", "NotifyDataSetChanged")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        list= ArrayList()
        preferencesHelper= SharedPreferencesHelper(this)
        setStatusBarColor(R.color.green)
        MyNotificationManager(this@ChatActivity, requestPermissionLauncher)

        mediaRecorder = MediaRecorder()
        mediaPlayer = MediaPlayer()

        userModel = intent.getParcelableExtra("userModel")
        myModel = intent.getParcelableExtra("myModel")!!
        fromRecentChat = intent.getBooleanExtra("fromRecentChat",false)


        lifecycleScope.launch(Dispatchers.IO) {
            myModel.chattingWith=userModel!!.key
            updateActivityStateAndChatKey(true,userModel!!.key)
            mainViewModel.getAnyModelFlow(USERS+"/"+userModel?.key,UserModel())
        }

        lifecycleScope.launch {
            mainViewModel.userFlow.collect{
                userModel?.chattingWith=it.chattingWith
            }
        }


        if (fromRecentChat){
            lifecycleScope.launch(Dispatchers.IO) {
                mainViewModel.updateNumberOfMessages(RECENTCHAT+"/"+myModel.key+"/"+userModel!!.key)
                clearNotifications(this@ChatActivity)
            }
        }

        binding.toolBarTitle.text = userModel?.fullName
        chatUploadPath = "room/"+getSortedKeys(userModel?.key!!,auth.currentUser!!.uid)

        binding.profileImg.loadImageViaLink(userModel!!.profileUrl)
        userUid=myModel.key

        binding.backPressImg.setOnClickListener {
            if (binding.reactionView.visibility== View.VISIBLE){
                hideReactionViews()
            }else{
                finish()
            }
        }

        binding.mainConstraint.setOnClickListener{
            hideReactionViews()
        }

        binding.recyclerView.setOnClickListener{
            hideReactionViews()
        }
        binding.toolBarTitle.setOnClickListener {
            val intent=Intent(this@ChatActivity,SeeUserProfileActivity::class.java)
            intent.putExtra("userModel",userModel)
            startActivity(intent)
            hideReactionViews()
        }


//      download manager
        val receiver = DownloadReceiver()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), RECEIVER_EXPORTED)
        }else{
            registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }

        val layoutManager = LinearLayoutManager(this@ChatActivity)
        binding.recyclerView.layoutManager = layoutManager

        adapter = ChatAdapter(this@ChatActivity, userUid, binding.dateTv,databaseReference,chatUploadPath,mainViewModel,lifecycleScope,binding.recyclerView,userModel?.key!!,layoutManager,storageViewModel,userModel!!,auth,myModel) { it, from, messageModel, position->

            if (from){

                val location = IntArray(2)
                it.getLocationOnScreen(location)
                val yPosition = location[1]-100
                val floatingView = binding.reactionView
                val layoutParams = floatingView.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.topMargin = yPosition
                floatingView.layoutParams = layoutParams

                floatingView.visibility = View.VISIBLE

                if (messageModel.message.isNotEmpty()){
                    binding.copyMessageImg.visibility= View.VISIBLE
                }else{
                    binding.copyMessageImg.visibility= View.GONE
                }

                binding.deleteMessageImg.visibility= View.VISIBLE

                binding.copyMessageImg.setOnClickListener {
                    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("message copied", messageModel.message)
                    clipboard.setPrimaryClip(clip)
                    hideReactionViews()
                    showToast("message copied.")
                }

                showReactionDialog(messageModel) {selectedReaction->
                    addReactionOnDB(messageModel, selectedReaction)
                }
                this.messageModel = messageModel
            }else {
                hideReactionViews()
            }

        }


        lifecycleScope.launch(Dispatchers.IO) {
            mainViewModel.isRecentChatUploaded.collect{
                if (it){
                    mainViewModel.updateNumberOfMessages(RECENTCHAT+"/"+myModel.key+"/"+userModel!!.key)
                    val isChatting=mainViewModel.isUserInActivity.value
                    if (myModel.key==userModel!!.chattingWith){
                        if (isChatting){
                            mainViewModel.updateNumberOfMessages(RECENTCHAT+"/"+userModel!!.key+"/"+myModel.key)
                        }
                    }
                    mainViewModel.isRecentChatUploaded.value=false
                }
            }
        }


        binding.recyclerView.adapter = adapter
        layoutManager.stackFromEnd=true

        lifecycleScope.launch {

            mainViewModel.collectAnyModel(chatUploadPath, MessageModel::class.java).collect {

                if (it.isNotEmpty()){
                    list=it as ArrayList
                }

                adapter.setList(it)
                adapter.notifyDataSetChanged()
                setAdapter(adapter)
            }
        }

        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                CoroutineScope(Dispatchers.Main).launch {
                    binding.dateTv.visibility = View.VISIBLE
                    delay(2000)
                    binding.dateTv.visibility = View.GONE
                }
            }
        })

        binding.deleteMessageImg.setOnClickListener {
            hideReactionViews()
            lifecycleScope.launch {
                if (userUid!=messageModel.senderUid){
                    deleteMessage(messageModel,false)
                    adapter.notifyDataSetChanged()
                }else{
                    deleteMessage(messageModel,true)
                    adapter.notifyDataSetChanged()
                }
            }
        }


        // swipe listener
        val swipeHandler = SwipeToRevealCallback(adapter) { messageModel ->

            referenceMessageModel=messageModel

            binding.edtLinear.setBackgroundResource(R.drawable.bottom_coners_round_edt_bac)
            binding.messageBox.showSoftKeyboard()

            binding.tvClearImg.setOnClickListener {
                binding.tvRefLinear.visibility= View.GONE
                binding.edtLinear.setBackgroundResource(R.drawable.rounded_bac)
                referenceMessageModel= MessageModel()
            }

            binding.ClearImgForImg.setOnClickListener {
                binding.imgRefConstraint.visibility= View.GONE
                binding.edtLinear.setBackgroundResource(R.drawable.rounded_bac)
                referenceMessageModel= MessageModel()
            }

            binding.ClearImgForVoice.setOnClickListener {
                binding.voiceRefConstraint.visibility= View.GONE
                binding.edtLinear.setBackgroundResource(R.drawable.rounded_bac)
                referenceMessageModel= MessageModel()
            }

            if (messageModel.message.isNotEmpty()){

                binding.tvRefLinear.visibility= View.VISIBLE
                binding.imgRefConstraint.visibility= View.GONE
                binding.documentImg.visibility= View.GONE
                binding.voiceRefConstraint.visibility= View.GONE

                binding.refMessageTv.text=messageModel.message
                binding.nameTv.text=myModel.fullName

            }else if (messageModel.imageUrl.isNotEmpty()){

                binding.imgRefConstraint.visibility= View.VISIBLE
                binding.tvRefLinear.visibility= View.GONE
                binding.documentImg.visibility= View.GONE
                binding.voiceRefConstraint.visibility= View.GONE

                binding.refNameForImg.text=myModel.fullName
                Glide.with(this@ChatActivity).load(messageModel.imageUrl).placeholder(R.drawable.photo).into(binding.refImg)
                binding.referenceImageType.setImageResource(R.drawable.photo)
                binding.typeTv.text="photo"

            }else if (messageModel.videoUrl.isNotEmpty()){

                binding.imgRefConstraint.visibility= View.VISIBLE
                binding.tvRefLinear.visibility= View.GONE
                binding.documentImg.visibility= View.GONE
                binding.voiceRefConstraint.visibility= View.GONE

                binding.refNameForImg.text=myModel.fullName
                binding.referenceImageType.setImageResource(R.drawable.video)
                binding.typeTv.text="video"
                binding.refImg.loadThumbnail(messageModel.videoUrl)

            }else if (messageModel.documentUrl.isNotEmpty()){

                binding.imgRefConstraint.visibility= View.GONE
                binding.voiceRefConstraint.visibility= View.GONE
                binding.tvRefLinear.visibility= View.VISIBLE
                binding.documentImg.visibility= View.VISIBLE

                binding.refMessageTv.text = messageModel.documentFileName.ifEmpty { "null" }

            }else if (messageModel.voiceUrl.isNotEmpty()){

                binding.voiceRefConstraint.visibility= View.VISIBLE
                binding.refNameForVoice.text=myModel.fullName

                binding.imgRefConstraint.visibility= View.GONE
                binding.tvRefLinear.visibility= View.GONE
                binding.documentImg.visibility= View.GONE

            }
        }


        // swipe listener
        val itemTouchHelper = ItemTouchHelper(swipeHandler)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)


        binding.cameraButton.setOnClickListener {

            val alert = AlertDialog.Builder(this@ChatActivity).setView(R.layout.image_selection_dialog).show()
            val cameraTv = alert.findViewById<TextView>(R.id.cameraTv)
            val galleryTv = alert.findViewById<TextView>(R.id.galleryTv)
            val selectVideo = alert.findViewById<TextView>(R.id.selectVideo)

            cameraTv?.setOnClickListener {
                capturePhoto()
                alert.dismiss()
            }

            galleryTv?.setOnClickListener {
                pickImageFromGallery(pickImageRequestCode,this@ChatActivity)
                alert.dismiss()
            }

            selectVideo?.setOnClickListener {
                pickVideo(pickVideoRequestCode, this@ChatActivity)
                alert.dismiss()
            }
        }

        binding.attachment.setOnClickListener {
            pickDocument(pickDocumentRequestCode,this@ChatActivity)
        }

        binding.messageBox.onTextChange {
            if (it.isNotEmpty()) {
                binding.voiceAndSendImage.setImageResource(R.drawable.baseline_send_24)
            } else {
                with(binding.voiceAndSendImage) {
                    setImageResource(R.drawable.baseline_keyboard_voice_24)
                }
            }
        }


        binding.voiceAndSendImage.setOnClickListener {
            val message = binding.messageBox.text.toString()

            if (message.isNotEmpty()) {
                lifecycleScope.launch {

                    withContext(Dispatchers.IO){
                        uploadToRecentChat(message,TEXT)
                    }

                    val key=databaseReference.push().key.toString()

                    val messageModel = MessageModel(
                        key = key,
                        senderName = "",
                        senderImageUrl = "",
                        senderPhone = "",
                        message = message,
                        timeStamp = System.currentTimeMillis(),
                        senderUid = userUid,
                        blockList = arrayListOf(""),
                        imageUrl = "",
                        voiceUrl = "",
                        documentUrl = "",
                        referenceMessageSenderName = myModel.fullName,
                        referenceMessage = if (::referenceMessageModel.isInitialized&&referenceMessageModel.message.isNotEmpty()){referenceMessageModel.message} else{""},
                        referenceMessageId = if (::referenceMessageModel.isInitialized&&referenceMessageModel.key.isNotEmpty()){referenceMessageModel.key} else{""},
                        referenceImgUrl = if (::referenceMessageModel.isInitialized&&referenceMessageModel.imageUrl.isNotEmpty()){referenceMessageModel.imageUrl} else{""},
                        referenceVideoUrl = if (::referenceMessageModel.isInitialized&&referenceMessageModel.videoUrl.isNotEmpty()){referenceMessageModel.videoUrl} else{""},
                        referenceDocumentName = if (::referenceMessageModel.isInitialized&&referenceMessageModel.documentFileName.isNotEmpty()){referenceMessageModel.documentFileName} else{""},
                        referenceVoiceUrl = if (::referenceMessageModel.isInitialized&&referenceMessageModel.voiceUrl.isNotEmpty()){referenceMessageModel.voiceUrl} else{""},
                    )

                    list.add(messageModel)
                    adapter.setList(list)
                    adapter.notifyDataSetChanged()

                    binding.messageBox.setText("")

                    referenceMessageModel=MessageModel()
                    binding.edtLinear.setBackgroundResource(R.drawable.rounded_bac)

                    binding.tvRefLinear.visibility= View.GONE
                    binding.imgRefConstraint.visibility= View.GONE
                    binding.voiceRefConstraint.visibility= View.GONE

                    val messageUploadResult = mainViewModel.uploadAnyModel(chatUploadPath, messageModel)

                    messageUploadResult.whenSuccess {

                    }

                    messageUploadResult.whenError {
                        showToast(it.toString())
                        Log.i("TAG", "onCreate:${it.message} ")
                    }
                    binding.recyclerView.scrollToPosition(list.size - 1)
                }

            }else {
                binding.voiceSenderLinearLayout.visibility = View.VISIBLE
                binding.linear02.visibility = View.GONE
                animateViewFromBottom(binding.voiceSenderLinearLayout)
                mediaRecorder = MediaRecorder()
                startRecording()
            }
        }


        binding.deleteImg.setOnClickListener {
            animateViewHideToBottom(binding.voiceSenderLinearLayout)
            binding.voiceSenderLinearLayout.visibility = View.GONE
            binding.linear02.visibility = View.VISIBLE
            stopRecording()
            val fileToDelete = File(filePath!!)
            if (fileToDelete.exists()) {
                if (fileToDelete.delete()) {
                    Log.i("FileDeleted", "File deleted: $filePath")
                } else {
                    Log.i("FileDeleted", "Unable to delete file: $filePath")
                }
            } else {
                Log.i("FileDeleted", "File does not exist: $filePath")
            }
        }


        binding.sendImg.setOnClickListener {


            stopRecording()

            binding.voiceSenderLinearLayout.visibility = View.GONE
            binding.linear02.visibility = View.VISIBLE
            animateViewHideToBottom(binding.voiceSenderLinearLayout)



            lifecycleScope.launch {
                withContext(Dispatchers.IO){
                    uploadToRecentChat("Voice", VOICE)
                }


                val uri=getUriOfTheFile(filePath!!)
                val firebaseUrlResult=uploadAudioToFirebase(uri)
                val key=databaseReference.push().key!!

                val messageModel = MessageModel(
                    key=key,
                    senderName = "",
                    senderImageUrl = "",
                    senderPhone = "",
                    timeStamp = System.currentTimeMillis(),
                    senderUid = userUid,
                    voiceUrl = uri.toString()
                )

                preferencesHelper.saveString(key, filePath!!)

                list.add(messageModel)
                adapter.setList(list)
                adapter.notifyDataSetChanged()

                firebaseUrlResult.whenSuccess {
                    lifecycleScope.launch {
                        val messageModelForUpload = MessageModel(
                            key=key,
                            senderName = "",
                            senderImageUrl = "",
                            senderPhone = "",
                            timeStamp = System.currentTimeMillis(),
                            senderUid = userUid,
                            voiceUrl = it
                        )
                        mainViewModel.uploadAnyModel(chatUploadPath, messageModelForUpload)
                        sendNotification("Voice")
                        adapter.notifyDataSetChanged()
                    }
                }
                firebaseUrlResult.whenError {
                    Toast.makeText(this@ChatActivity,it.message.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }

        val permission = android.Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission, android.Manifest.permission.READ_EXTERNAL_STORAGE, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.CAMERA), 11)
        }
    }


    fun startRecording() {
        binding.audioTracker.recreate()
        filePath = "${filesDir?.absolutePath}/chatsphere.3gp"
        try {
            Toast.makeText(this@ChatActivity, "Recording Started", Toast.LENGTH_SHORT).show()
            mediaRecorder!!.apply {
                reset()
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.DEFAULT)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(filePath)
                prepare()
                start()
                handler.postDelayed(object : Runnable {
                    override fun run() {
                        val currentMaxAmplitude = mediaRecorder?.maxAmplitude ?: 0
                        binding.audioTracker.update(currentMaxAmplitude)
                        handler.postDelayed(this, 300)
                    }
                }, 300)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            reset()
            release()
        }
        binding.audioTracker.recreate()
        handler.removeCallbacksAndMessages(null)
        mediaRecorder = null
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                pickImageRequestCode -> {
                    val uri = data!!.data
                    if (uri!=null){
                        gotoEditActivity(this@ChatActivity,uri,"DATA",cropImageRequestCode)
                    }
                }

                cameraImageRequestCode -> {
                    val bitmap = data!!.extras?.get("data") as Bitmap
                    CoroutineScope(Dispatchers.Main).launch {
                        val uri=bitmapToUri(this@ChatActivity,bitmap)
                        if (uri!=null){
                            gotoEditActivity(this@ChatActivity,uri,"DATA",cropImageRequestCode)
                        }
                    }
                }

                pickVideoRequestCode -> {
                    val uri = data!!.data!!
                    uploadVideo(uri.toString())
                    val takeFlags = data.flags?.and((Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                    if (takeFlags != null) {
                        contentResolver.takePersistableUriPermission(uri, takeFlags)
                    }
                }

                pickDocumentRequestCode->{
                    val selectedPdfUri = data!!.data!!
                    val takeFlags = data.flags.and((Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION))
                    contentResolver.takePersistableUriPermission(selectedPdfUri, takeFlags)
                    val fileName= getFileName(selectedPdfUri,this@ChatActivity)
                    uploadDocument(selectedPdfUri.toString(),fileName)
                }

                cropImageRequestCode->{
                    val bundleUri = data?.getStringExtra("RESULT")
                    if (bundleUri !== null) {
                        uploadImage(bundleUri.toString())
                    }
                }

            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun capturePhoto() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, cameraImageRequestCode)
    }


    suspend fun bitmapToUri(context: Context, bitmap: Bitmap): Uri? {
        val file = File(context.externalCacheDir, "temp_img.jpg")
        try {
            val outputStream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        return Uri.fromFile(file)
    }


    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("NotifyDataSetChanged")
    fun uploadImage(uri:String){
        if (uri.isNotEmpty()) {

            uploadToRecentChat("Image",IMAGE)

            val key=databaseReference.push().key.toString()
            val time=System.currentTimeMillis()

            val intent = Intent(this, UploadImageService::class.java)

            intent.putExtra("imageUri", uri)
            intent.putExtra("chatUploadPath", chatUploadPath)
            intent.putExtra("userUid", userUid)
            intent.putExtra("time", time)
            intent.putExtra("key", key)
            intent.putExtra("myModel", myModel)
            intent.putExtra("userModel", userModel)

            val messageModel = MessageModel(
                key=key,
                senderName = "",
                senderImageUrl = "",
                senderPhone = "",
                timeStamp = System.currentTimeMillis(),
                senderUid = userUid,
                imageUrl = uri,
            )

            preferencesHelper.saveString(key,uri)

            list.add(messageModel)
            adapter.setList(list)
            adapter.notifyDataSetChanged()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, intent)
            } else {
                startService(intent)
            }
        } else {
            Toast.makeText(this@ChatActivity, "Video Not selected", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("NotifyDataSetChanged")
    fun uploadVideo(uri:String){
        if (uri.isNotEmpty()) {
            uploadToRecentChat("Video",VIDEO)

            val key=databaseReference.push().key.toString()
            val time=System.currentTimeMillis()

            val intent = Intent(this, UploadVideoService::class.java)
            intent.putExtra("videoUri", uri)
            intent.putExtra("chatUploadPath", chatUploadPath)
            intent.putExtra("userUid", userUid)
            intent.putExtra("key", key)
            intent.putExtra("timestamp", time)

            val messageModel = MessageModel(
                key=key,
                senderName = "",
                senderImageUrl = "",
                senderPhone = "",
                timeStamp =time ,
                senderUid = userUid,
                videoUrl = uri,
            )

            preferencesHelper.saveString(key,uri)


            list.add(messageModel)
            adapter.setList(list)
            adapter.notifyDataSetChanged()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, intent)
            } else {
                startService(intent)
            }
        }else {
            Toast.makeText(this@ChatActivity, "Video Not selected", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun uploadDocument(uri:String, fileName:String){
        if (uri.isNotEmpty()) {
            uploadToRecentChat(fileName, DOCUMENT)

            val key=databaseReference.push().key.toString()
            val time=System.currentTimeMillis()

            val intent = Intent(this, UploadDocumentService::class.java)
            intent.putExtra("docxUri", uri)
            intent.putExtra("chatUploadPath", chatUploadPath)
            intent.putExtra("userUid", userUid)
            intent.putExtra("docxFileName", fileName)
            intent.putExtra("docxFileName", fileName)
            intent.putExtra("key", key)
            intent.putExtra("time", time)

            val messageModel = MessageModel(
                key = key,
                senderName = "",
                senderImageUrl = "",
                senderPhone = "",
                timeStamp = time,
                senderUid = userUid,
                documentUrl = uri,
                documentFileName = fileName
            )

            preferencesHelper.saveString(key,uri)
            list.add(messageModel)
            adapter.setList(list)
            adapter.notifyDataSetChanged()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, intent)
            } else {
                startService(intent)
            }
        }else {
            Toast.makeText(this@ChatActivity, "document not selected", Toast.LENGTH_SHORT).show()
        }
    }



    fun showReactionDialog(data: MessageModel,callBack:(Int)->Unit){

        val like_anim=binding.likeAnim
        val heart_anim=binding.heartAnim
        val surprise_anim=binding.surpriseAnim
        val happy_anim=binding.happyAnim
        val angry_anim=binding.angryAnim

        like_anim.setOnClickListener {
            callBack.invoke(1)
            hideReactionViews()
        }

        heart_anim.setOnClickListener {
            callBack.invoke(2)
            hideReactionViews()
        }

        surprise_anim.setOnClickListener {
            callBack.invoke(3)
            hideReactionViews()
        }

        happy_anim.setOnClickListener {
            callBack.invoke(4)
            hideReactionViews()
        }

        angry_anim.setOnClickListener {
            callBack.invoke(5)
            hideReactionViews()
        }

        val animationDuration:Long=800
        like_anim.zoomIn(animationDuration)
        heart_anim.zoomIn(animationDuration)
        surprise_anim.zoomIn(animationDuration)
        happy_anim.zoomIn(animationDuration)
        angry_anim.zoomIn(animationDuration)

    }


    fun getReaction(reactionId:Int):String{
        return when(reactionId){
            1-> "like"
            2-> "heart"
            3-> "surprise"
            4-> "happy"
            5-> "angry"
            else-> ""
        }
    }

    fun hideReactionViews(){
        binding.reactionView.visibility= View.GONE
        binding.copyMessageImg.visibility= View.GONE
        binding.deleteMessageImg.visibility= View.GONE
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun addReactionOnDB(messageModel: MessageModel, selectedReaction:Int){

        var currentReactionPath="$chatUploadPath/${messageModel.key}"
        var previousReactionPath="$chatUploadPath/${messageModel.key}"
        val reactionDetailsPath="reactionsDetails/${getSortedKeys(userModel?.key!!,auth.currentUser!!.uid)}/${messageModel.key}/${auth.currentUser!!.uid}"

        GlobalScope.launch {

            val whichReaction = databaseReference.child("$reactionDetailsPath/reactionId").get().await()
            val reactionId = whichReaction.getValue(Int::class.java)
            reactionId?.logT("ReactionId")

            if (reactionId!=null){

                val reaction=getReaction(reactionId)
                var previousReact=0

                reaction.logT("reaction")

                when(reaction){

                    "like"->{
                        previousReactionPath+="/like"
                        previousReact=messageModel.like
                    }

                    "heart"->{
                        previousReactionPath+= "/heart"
                        previousReact=messageModel.heart
                    }

                    "surprise"->{
                        previousReactionPath+= "/surprise"
                        previousReact=messageModel.surprise
                    }

                    "happy"->{
                        previousReactionPath+= "/happy"
                        previousReact=messageModel.happy
                    }

                    "angry"->{
                        previousReactionPath+="/angry"
                        previousReact=messageModel.angry
                    }
                }

                when (selectedReaction) {
                    1 -> {
                        currentReactionPath+="/like"
                    }
                    2 -> {
                        currentReactionPath+="/heart"
                    }
                    3 -> {
                        currentReactionPath+="/surprise"
                    }
                    4 -> {
                        currentReactionPath+="/happy"
                    }
                    5 -> {
                        currentReactionPath+="/angry"
                    }
                }

                val currentSnap=databaseReference.child(currentReactionPath).get().await()
                val currentValue=currentSnap.getValue(Int::class.java)

                if (currentValue!=previousReact){
                    databaseReference.child(previousReactionPath).setValue(previousReact-1).await()

                    if (currentValue!=null){
                        databaseReference.child(currentReactionPath).setValue(currentValue+1).await()
                    }else{
                        databaseReference.child(currentReactionPath).setValue(1).await()
                    }
                    databaseReference.child("$reactionDetailsPath/reactionId").setValue(selectedReaction).await()
                }

            }else{

                when (selectedReaction) {
                    1 -> {
                        currentReactionPath+="/like"
                    }
                    2 -> {
                        currentReactionPath+="/heart"
                    }
                    3 -> {
                        currentReactionPath+="/surprise"
                    }
                    4 -> {
                        currentReactionPath+="/happy"
                    }
                    5 -> {
                        currentReactionPath+="/angry"
                    }
                }

                val currentSnap=databaseReference.child(currentReactionPath).get().await()
                val currentValue=currentSnap.getValue(Int::class.java)

                if (currentValue!=null){
                    databaseReference.child(currentReactionPath).setValue(currentValue+1).await()
                }else{
                    databaseReference.child(currentReactionPath).setValue(1).await()
                }
                databaseReference.child(reactionDetailsPath).setValue(ReactionModel(auth.currentUser!!.uid,selectedReaction).shrink()).await()
            }
        }
    }



    @SuppressLint("NotifyDataSetChanged")
    suspend fun deleteMessage(data: MessageModel, isSenderAndReceiver:Boolean) {
        val scope=lifecycleScope
        NewUtils.showMessageDeleteDialog(this@ChatActivity, isSenderAndReceiver) {

            if (isSenderAndReceiver) { // will run only for sender
                if (it == 1) {

                    scope.launch {
                        databaseReference.child(chatUploadPath + "/" + data.key).removeValue().await()
                        showToast("message deleted from everyone.")
                    }

                    scope.launch {

                        if (data.imageUrl.isNotEmpty()) {
                            storageViewModel.deleteDocumentToFirebaseStorage(data.imageUrl)
                        } else if (data.videoUrl.isNotEmpty()) {
                            storageViewModel.deleteDocumentToFirebaseStorage(data.videoUrl)

                        } else if (data.documentUrl.isNotEmpty()) {
                            storageViewModel.deleteDocumentToFirebaseStorage(data.documentUrl)

                        } else if (data.voiceUrl.isNotEmpty()) {
                            storageViewModel.deleteDocumentToFirebaseStorage(data.voiceUrl)
                        }
                    }

                } else if (it == 2) {
                    scope.launch {
                        val map = HashMap<String, Any>()
                        map["deleteMessageFromMe"] = true
                        databaseReference.child(chatUploadPath + "/" + data.key).updateChildren(map).await()
                        showToast( "message deleted.")
                    }
                }
            } else { // will run for receiver

                scope.launch {
                    data.deletedMessagesList.add(userUid)
                    val map = mapOf("deletedMessagesList" to data.deletedMessagesList)
                    databaseReference.child("$chatUploadPath/${data.key}").updateChildren(map).await()
                    showToast( "Message deleted")
                    adapter.notifyDataSetChanged()
                }

            }
        }
    }


    fun uploadToRecentChat(recentMessage:String,messageType:String){

        lifecycleScope.launch(Dispatchers.IO) {

            sendNotification(recentMessage)

            val numberOfMessagesForReceiver=mainViewModel.getAnyData(RECENTCHAT+"/"+userModel!!.key+"/"+myModel.key, RecentChatModel::class.java)?.numberOfMessages?:0

            val timeStamp=System.currentTimeMillis()
            // who will receive
            val recentChatModelOfReceiver=RecentChatModel(userModel!!.key,recentMessage, messageType,0,timeStamp)
            // who will send
            val recentChatModelOfSender=RecentChatModel(myModel.key,recentMessage, messageType,numberOfMessagesForReceiver+1,timeStamp)

            mainViewModel.uploadAnyModel(RECENTCHAT+"/"+myModel.key,recentChatModelOfReceiver)
            mainViewModel.uploadAnyModel(RECENTCHAT+"/"+userModel!!.key,recentChatModelOfSender)
            mainViewModel.isRecentChatUploaded.value=true

        }
    }

    fun sendNotification(message:String){
        lifecycleScope.launch {
            Log.i("TAG", "my model chatting id :${myModel.chattingWith}")
            Log.i("TAG", "user model chatting id :${userModel!!.chattingWith}")
            if (myModel.key!=userModel!!.chattingWith){
                myModel.apply {
                    val accessToken= getAccessToken(this@ChatActivity)
                    if (!accessToken.isNullOrEmpty()){
                        SendNotification.sendMessageNotification(fullName,message,userModel!!.token,accessToken)
                    }else{
                        showToast("your access token is not found")
                    }
                }
            }
        }
    }

    fun updateActivityStateAndChatKey(bol:Boolean, chattingWithKey:String=""){
        lifecycleScope.launch(Dispatchers.IO) {
            val map=HashMap<String,Any>()
            map[ACTIVITYSTATEOFTHEUSER] = bol
            map[CHATTINGWITH] = chattingWithKey
            mainViewModel.uploadMap(USERS+"/"+myModel.key,map)
        }
    }

    override fun onPause() {
        super.onPause()
        updateActivityStateAndChatKey(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        updateActivityStateAndChatKey(false)
    }

    override fun onResume() {
        super.onResume()
        updateActivityStateAndChatKey(true,userModel!!.key)
    }

}