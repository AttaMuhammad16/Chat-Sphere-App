package com.atta.chatspherapp.adapters

import android.animation.Animator
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.atta.chatspherapp.R
import com.atta.chatspherapp.databinding.ReactionSampleRowBinding
import com.atta.chatspherapp.models.MessageModel
import com.atta.chatspherapp.models.ReactionModel
import com.atta.chatspherapp.models.UserModel
import com.atta.chatspherapp.ui.activities.room.ChatActivity
import com.atta.chatspherapp.ui.activities.room.PhotoViewActivity
import com.atta.chatspherapp.ui.activities.room.VideosActivity
import com.atta.chatspherapp.ui.viewmodel.MainViewModel
import com.atta.chatspherapp.ui.viewmodel.StorageViewModel
import com.atta.chatspherapp.utils.Constants.RECEIVER_DOCUMENT_MESSAGE
import com.atta.chatspherapp.utils.Constants.RECEIVER_IMAGE_MESSAGE
import com.atta.chatspherapp.utils.Constants.RECEIVER_VIDEO_MESSAGE
import com.atta.chatspherapp.utils.Constants.RECEIVER_VIEW_SIMPLE_MESSAGE
import com.atta.chatspherapp.utils.Constants.RECEIVER_VOICE_MESSAGE
import com.atta.chatspherapp.utils.Constants.SENDER_DOCUMENT_MESSAGE
import com.atta.chatspherapp.utils.Constants.SENDER_IMAGE_MESSAGE
import com.atta.chatspherapp.utils.Constants.SENDER_VIDEO_MESSAGE
import com.atta.chatspherapp.utils.Constants.SENDER_VIEW_SIMPLE_MESSAGE
import com.atta.chatspherapp.utils.Constants.SENDER_VOICE_MESSAGE
import com.atta.chatspherapp.utils.Constants.USERS
import com.atta.chatspherapp.utils.NewUtils.downloadAudio
import com.atta.chatspherapp.utils.NewUtils.downloadVideo
import com.atta.chatspherapp.utils.NewUtils.formatDateFromMillis
import com.atta.chatspherapp.utils.NewUtils.getFormattedDateAndTime
import com.atta.chatspherapp.utils.NewUtils.getSortedKeys
import com.atta.chatspherapp.utils.NewUtils.loadImageFromResource
import com.atta.chatspherapp.utils.NewUtils.loadImageViaLink
import com.atta.chatspherapp.utils.NewUtils.loadThumbnail
import com.atta.chatspherapp.utils.NewUtils.openDocument
import com.atta.chatspherapp.utils.NewUtils.setData
import com.atta.chatspherapp.utils.NewUtils.showToast
import com.atta.chatspherapp.utils.SharedPreferencesHelper
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject


class ChatAdapter(

    private val context: ChatActivity,
    private val myUid: String,
    private val date: TextView,
    private val databaseReference: DatabaseReference,
    private val chatPath: String,
    private val mainViewModel: MainViewModel,
    var scope: CoroutineScope,
    var recyclerView: RecyclerView,
    var subjectKey: String,
    var layoutManager: LinearLayoutManager,
    var storageViewModel: StorageViewModel,
    var userModel: UserModel,
    var auth:FirebaseAuth,
    var myModel:UserModel,
    var longClicked: (View, Boolean, MessageModel, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var outerMap:MutableMap<String,Map<String,String>> = mutableMapOf()
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

//    var myMediaPlayer = MediaPlayerHelper.getInstance(context)
    var preferencesHelper: SharedPreferencesHelper = SharedPreferencesHelper(context)
    var mylist:List<MessageModel> = listOf()
    lateinit var currentSeekBar:SeekBar




    private var currentlyPlayingMediaPlayer: MediaPlayer? = null
    private var currentPlayingButton: ImageView? = null
    private val handler = Handler(Looper.getMainLooper())


    // for text
    class SenderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val senderMessageText: TextView = view.findViewById(R.id.message)
        val senderTime: TextView = view.findViewById(R.id.message_time)
        val linearSender: LinearLayout = view.findViewById(R.id.message_linear)

        val reference_card: CardView = view.findViewById(R.id.reference_card)
        val messageOwnerNameTv: TextView = view.findViewById(R.id.messageOwnerNameTv)

        val refMessageLinear: LinearLayout = view.findViewById(R.id.refMessageLinear)
        val documentImg: ImageView = view.findViewById(R.id.documentImg)
        val referenceMessageTv: TextView = view.findViewById(R.id.referenceMessageTv)

        val photoLinear: LinearLayout = view.findViewById(R.id.photoLinear)
        val referenceImage: ImageView = view.findViewById(R.id.referenceImage)
        val refImgType: ImageView = view.findViewById(R.id.refImgType)
        val refType: TextView = view.findViewById(R.id.refType)
        val refImgCard: CardView = view.findViewById(R.id.refImgCard)


        var feelingCard: CardView = view.findViewById(R.id.feelingCard)
        var likeImg: ImageView = view.findViewById(R.id.likeImg)
        var heartImg: ImageView = view.findViewById(R.id.heartImg)
        var happyImg: ImageView = view.findViewById(R.id.happyImg)
        var angryImg: ImageView = view.findViewById(R.id.angryImg)
        var surpriseImg: ImageView = view.findViewById(R.id.surpriseImg)
        var countTv: TextView = view.findViewById(R.id.countTv)

    }

    // for text
    class ReceiverViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val receiverMessageText: TextView = view.findViewById(R.id.message)
        val receiverTime: TextView = view.findViewById(R.id.message_time)
        val linearReceiver: LinearLayout = view.findViewById(R.id.message_linear)

        val reference_card: CardView = view.findViewById(R.id.reference_card)
        val messageOwnerNameTv: TextView = view.findViewById(R.id.messageOwnerNameTv)

        val referenceMessageTv: TextView = view.findViewById(R.id.referenceMessageTv)
        val refMessageLinear: LinearLayout = view.findViewById(R.id.refMessageLinear)
        val documentImg: ImageView = view.findViewById(R.id.documentImg)

        val photoLinear: LinearLayout = view.findViewById(R.id.photoLinear)
        val referenceImage: ImageView = view.findViewById(R.id.referenceImage)
        val refImgType: ImageView = view.findViewById(R.id.refImgType)
        val refType: TextView = view.findViewById(R.id.refType)
        val refImgCard: CardView = view.findViewById(R.id.refImgCard)



        var feelingCard: CardView = view.findViewById(R.id.feelingCard)
        var likeImg: ImageView = view.findViewById(R.id.likeImg)
        var heartImg: ImageView = view.findViewById(R.id.heartImg)
        var happyImg: ImageView = view.findViewById(R.id.happyImg)
        var angryImg: ImageView = view.findViewById(R.id.angryImg)
        var surpriseImg: ImageView = view.findViewById(R.id.surpriseImg)
        var countTv: TextView = view.findViewById(R.id.countTv)

    }


    // for voice
    class VoiceSenderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val playButton: ImageView = view.findViewById(R.id.playButton)
        val seekBar: SeekBar = view.findViewById(R.id.seekBar)
        val voiceLength: TextView = view.findViewById(R.id.voiceLength)
        val voiceSendTime: TextView = view.findViewById(R.id.voiceSendTime)
        val linearSender: LinearLayout = view.findViewById(R.id.linearSender)

        var feelingCard: CardView = view.findViewById(R.id.feelingCard)
        var likeImg: ImageView = view.findViewById(R.id.likeImg)
        var heartImg: ImageView = view.findViewById(R.id.heartImg)
        var happyImg: ImageView = view.findViewById(R.id.happyImg)
        var angryImg: ImageView = view.findViewById(R.id.angryImg)
        var surpriseImg: ImageView = view.findViewById(R.id.surpriseImg)
        var countTv: TextView = view.findViewById(R.id.countTv)

    }
    class VoiceReceiverViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val playButton: ImageView = view.findViewById(R.id.playButton)
        val seekBar: SeekBar = view.findViewById(R.id.seekBar)
        val voiceLength: TextView = view.findViewById(R.id.voiceLength)
        val voiceSendTime: TextView = view.findViewById(R.id.voiceSendTime)
        val linearReceiver: LinearLayout = view.findViewById(R.id.linearReceiver)

        var feelingCard: CardView = view.findViewById(R.id.feelingCard)
        var likeImg: ImageView = view.findViewById(R.id.likeImg)
        var heartImg: ImageView = view.findViewById(R.id.heartImg)
        var happyImg: ImageView = view.findViewById(R.id.happyImg)
        var angryImg: ImageView = view.findViewById(R.id.angryImg)
        var surpriseImg: ImageView = view.findViewById(R.id.surpriseImg)
        var countTv: TextView = view.findViewById(R.id.countTv)

    }


    // for image
    class ImageSenderViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        val senderImageView: ImageView = view.findViewById(R.id.senderImageView)
        val senderTime: TextView = view.findViewById(R.id.sender_time)
        val linearSender: LinearLayout = view.findViewById(R.id.linearSender)

        var feelingCard: CardView = view.findViewById(R.id.feelingCard)
        var likeImg: ImageView = view.findViewById(R.id.likeImg)
        var heartImg: ImageView = view.findViewById(R.id.heartImg)
        var happyImg: ImageView = view.findViewById(R.id.happyImg)
        var angryImg: ImageView = view.findViewById(R.id.angryImg)
        var surpriseImg: ImageView = view.findViewById(R.id.surpriseImg)
        var countTv: TextView = view.findViewById(R.id.countTv)

    }

    class ImageReceiverViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val receiverImageView: ImageView = view.findViewById(R.id.receiverImageView)
        val receiverTime: TextView = view.findViewById(R.id.receiver_timeTv)
        val linearReceiver: LinearLayout = view.findViewById(R.id.linearReceiver)

        var feelingCard: CardView = view.findViewById(R.id.feelingCard)
        var likeImg: ImageView = view.findViewById(R.id.likeImg)
        var heartImg: ImageView = view.findViewById(R.id.heartImg)
        var happyImg: ImageView = view.findViewById(R.id.happyImg)
        var angryImg: ImageView = view.findViewById(R.id.angryImg)
        var surpriseImg: ImageView = view.findViewById(R.id.surpriseImg)
        var countTv: TextView = view.findViewById(R.id.countTv)
    }


    //for video
    class VideoSenderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val senderVideThumbnail: ImageView = view.findViewById(R.id.senderVideThumbnail)
        val senderTime: TextView = view.findViewById(R.id.sender_time)
        val playVideoImg: ImageView = view.findViewById(R.id.playImg)
        val downloadFileImg: ImageView = view.findViewById(R.id.downloadFileImg)
        val circularProgressIndicator: ProgressBar = view.findViewById(R.id.circularProgressIndicator)
        val linearSender: LinearLayout = view.findViewById(R.id.linearSender)

        var feelingCard: CardView = view.findViewById(R.id.feelingCard)
        var likeImg: ImageView = view.findViewById(R.id.likeImg)
        var heartImg: ImageView = view.findViewById(R.id.heartImg)
        var happyImg: ImageView = view.findViewById(R.id.happyImg)
        var angryImg: ImageView = view.findViewById(R.id.angryImg)
        var surpriseImg: ImageView = view.findViewById(R.id.surpriseImg)
        var countTv: TextView = view.findViewById(R.id.countTv)


    }

    class VideoReceiverViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val receiverThumbnailImageView: ImageView = view.findViewById(R.id.receiverThumbnailImageView)
        val downloadFileImg: ImageView = view.findViewById(R.id.downloadFileImg)
        val receiverTime: TextView = view.findViewById(R.id.receiver_timeTv)
        val circularProgressIndicator: ProgressBar = view.findViewById(R.id.circularProgressIndicator)
        val playImg: ImageView = view.findViewById(R.id.playImg)
        val linearReceiver: LinearLayout = view.findViewById(R.id.linearReceiver)


        var feelingCard: CardView = view.findViewById(R.id.feelingCard)
        var likeImg: ImageView = view.findViewById(R.id.likeImg)
        var heartImg: ImageView = view.findViewById(R.id.heartImg)
        var happyImg: ImageView = view.findViewById(R.id.happyImg)
        var angryImg: ImageView = view.findViewById(R.id.angryImg)
        var surpriseImg: ImageView = view.findViewById(R.id.surpriseImg)
        var countTv: TextView = view.findViewById(R.id.countTv)

    }
    // for document
    class DocumentSenderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val file_name_tv: TextView = view.findViewById(R.id.file_name_tv)
        val downloadImg: ImageView = view.findViewById(R.id.downloadImg)
        val send_time_tv: TextView = view.findViewById(R.id.send_time_tv)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val linearSender: LinearLayout = view.findViewById(R.id.linearSender)

        var feelingCard: CardView = view.findViewById(R.id.feelingCard)
        var likeImg: ImageView = view.findViewById(R.id.likeImg)
        var heartImg: ImageView = view.findViewById(R.id.heartImg)
        var happyImg: ImageView = view.findViewById(R.id.happyImg)
        var angryImg: ImageView = view.findViewById(R.id.angryImg)
        var surpriseImg: ImageView = view.findViewById(R.id.surpriseImg)
        var countTv: TextView = view.findViewById(R.id.countTv)
    }

    class DocumentReceiverViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val file_name_tv: TextView = view.findViewById(R.id.file_name_tv)
        val downloadImg: ImageView = view.findViewById(R.id.downloadImg)
        val receiver_timeTv: TextView = view.findViewById(R.id.receiver_timeTv)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val linearReceiver: LinearLayout = view.findViewById(R.id.linearReceiver)

        var feelingCard: CardView = view.findViewById(R.id.feelingCard)
        var likeImg: ImageView = view.findViewById(R.id.likeImg)
        var heartImg: ImageView = view.findViewById(R.id.heartImg)
        var happyImg: ImageView = view.findViewById(R.id.happyImg)
        var angryImg: ImageView = view.findViewById(R.id.angryImg)
        var surpriseImg: ImageView = view.findViewById(R.id.surpriseImg)
        var countTv: TextView = view.findViewById(R.id.countTv)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {

            SENDER_VIEW_SIMPLE_MESSAGE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.sender_simple_text_sample_row, parent, false)
                SenderViewHolder(view)
            }
            RECEIVER_VIEW_SIMPLE_MESSAGE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.receiver_simple_text_sample_row, parent, false)
                ReceiverViewHolder(view)
            }

            SENDER_VOICE_MESSAGE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.sender_voice_message_sample_row, parent, false)
                VoiceSenderViewHolder(view)
            }

            RECEIVER_VOICE_MESSAGE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.receiver_voice_message_sample_row, parent, false)
                VoiceReceiverViewHolder(view)
            }


            SENDER_IMAGE_MESSAGE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.sender_image_sample_row, parent, false)
                ImageSenderViewHolder(view)
            }
            RECEIVER_IMAGE_MESSAGE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.receiver_image_sample_row, parent, false)
                ImageReceiverViewHolder(view)
            }

            SENDER_VIDEO_MESSAGE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.sender_video_sample_row, parent, false)
                VideoSenderViewHolder(view)
            }

            RECEIVER_VIDEO_MESSAGE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.receiver_video_sample_row, parent, false)
                VideoReceiverViewHolder(view)
            }


            SENDER_DOCUMENT_MESSAGE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.sender_document_sample_row, parent, false)
                DocumentSenderViewHolder(view)
            }

            RECEIVER_DOCUMENT_MESSAGE -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.receiver_document_sample_row, parent, false)
                DocumentReceiverViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }


    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged", "SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val data = mylist[position]
        date.text=formatDateFromMillis(data.timeStamp)

        if (position!=0){
            val layoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.topMargin = 0
            holder.itemView.layoutParams = layoutParams

            if (mylist[position-1].senderUid!=mylist[position].senderUid){
                if (mylist[position-1].senderUid!=myUid){
                    val receiverLayoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
                    receiverLayoutParams.topMargin = 10
                    holder.itemView.layoutParams = receiverLayoutParams
                }else{
                    val receiverLayoutParams = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
                    receiverLayoutParams.topMargin = 10
                    holder.itemView.layoutParams = receiverLayoutParams
                }
            }
        }


        holder.itemView.setOnLongClickListener { v ->
            longClicked.invoke(v,true,data,position)
            true
        }

        holder.itemView.setOnClickListener { v ->
            longClicked.invoke(v,false,data,position)
        }


        when (holder) {

            // for text
            is SenderViewHolder -> {

                holder.senderMessageText.text = data.message
                holder.senderTime.text = data.formattedTime

                holder.feelingCard.setOnClickListener{
                    showReactedBottomSheet(data)
                }

                if (data.senderUid==myUid){
                    holder.messageOwnerNameTv.text="you"
                }else{
                    holder.messageOwnerNameTv.text=data.referenceMessageSenderName
                }

                if(data.referenceMessage.isNotEmpty()){
                    holder.reference_card.visibility=View.VISIBLE

                    holder.referenceMessageTv.visibility=View.VISIBLE
                    holder.refMessageLinear.visibility=View.VISIBLE
                    holder.documentImg.visibility=View.GONE

                    holder.photoLinear.visibility=View.GONE
                    holder.referenceImage.visibility=View.GONE
                    holder.refImgCard.visibility=View.GONE

                    holder.referenceMessageTv.text=data.referenceMessage

                }else if (data.referenceImgUrl.isNotEmpty()){
                    holder.reference_card.visibility=View.VISIBLE


                    holder.referenceMessageTv.visibility=View.GONE
                    holder.refMessageLinear.visibility=View.GONE
                    holder.documentImg.visibility=View.GONE

                    holder.photoLinear.visibility=View.VISIBLE
                    holder.referenceImage.visibility=View.VISIBLE
                    holder.refImgCard.visibility=View.VISIBLE
                    holder.refImgType.setImageResource(R.drawable.photo)
                    holder.refType.text="photo"
                    Glide.with(context).load(data.referenceImgUrl).placeholder(R.drawable.photo).into(holder.referenceImage)

                }else if (data.referenceVideoUrl.isNotEmpty()){
                    holder.reference_card.visibility=View.VISIBLE

                    holder.referenceMessageTv.visibility=View.GONE
                    holder.refMessageLinear.visibility=View.GONE
                    holder.documentImg.visibility=View.GONE

                    holder.photoLinear.visibility=View.VISIBLE
                    holder.referenceImage.visibility=View.VISIBLE
                    holder.refImgCard.visibility=View.VISIBLE

                    holder.refImgType.setImageResource(R.drawable.video)
                    holder.refType.text="video"
                    holder.referenceImage.loadThumbnail(data.referenceVideoUrl)

                }else if (data.referenceDocumentName.isNotEmpty()){
                    holder.reference_card.visibility=View.VISIBLE

                    holder.referenceMessageTv.text=data.referenceDocumentName
                    holder.documentImg.setImageResource(R.drawable.file)

                    holder.refMessageLinear.visibility=View.VISIBLE
                    holder.referenceMessageTv.visibility=View.VISIBLE
                    holder.documentImg.visibility=View.VISIBLE

                    holder.photoLinear.visibility=View.GONE
                    holder.referenceImage.visibility=View.GONE
                    holder.refImgCard.visibility=View.GONE


                }else if(data.referenceVoiceUrl.isNotEmpty()){
                    holder.reference_card.visibility=View.VISIBLE

                    holder.referenceMessageTv.text="Voice message"
                    holder.documentImg.setImageResource(R.drawable.baseline_keyboard_voice_24)

                    holder.refMessageLinear.visibility=View.VISIBLE
                    holder.referenceMessageTv.visibility=View.VISIBLE
                    holder.documentImg.visibility=View.VISIBLE


                    holder.photoLinear.visibility=View.GONE
                    holder.referenceImage.visibility=View.GONE
                    holder.refImgCard.visibility=View.GONE

                }else{

                    holder.referenceMessageTv.visibility=View.GONE
                    holder.refMessageLinear.visibility=View.GONE
                    holder.documentImg.visibility=View.GONE

                    holder.photoLinear.visibility=View.GONE
                    holder.referenceImage.visibility=View.GONE
                    holder.reference_card.visibility=View.GONE
                }





                holder.reference_card.setOnLongClickListener { v ->
                    longClicked.invoke(v,true,data,position)
                    true
                }

                holder.reference_card.setOnClickListener {
                    if (data.referenceMessageId.isNotEmpty()){
                        scrollToMessage(data.referenceMessageId,position)
                    }else{
                        showToast(context,"Reference id not found")
                    }
                }

//                holder.reference_card.setOnClickListener { v ->
//                    longClicked.invoke(v,false,data,position)
//                }

                holder.feelingCard.visibility = View.GONE
                holder.countTv.visibility = View.GONE
                holder.likeImg.visibility = View.GONE
                holder.heartImg.visibility = View.GONE
                holder.surpriseImg.visibility = View.GONE
                holder.happyImg.visibility = View.GONE
                holder.angryImg.visibility = View.GONE


                if (data.deleteMessageFromMe){
                    holder.linearSender.visibility=View.GONE
                }else{
                    holder.linearSender.visibility=View.VISIBLE
                    val count=data.like+data.heart+data.surprise+data.happy+data.angry
                    if (count>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.countTv.visibility = View.VISIBLE
                        holder.countTv.text = count.toString()
                    }

                    if (data.like>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.likeImg.visibility = View.VISIBLE
                    }

                    if (data.heart>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.heartImg.visibility = View.VISIBLE
                    }

                    if (data.surprise>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.surpriseImg.visibility = View.VISIBLE
                    }

                    if (data.happy>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.happyImg.visibility = View.VISIBLE
                    }

                    if (data.angry>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.angryImg.visibility = View.VISIBLE
                    }
                }
            }

            is ReceiverViewHolder -> {

                holder.receiverMessageText.text = data.message
                holder.receiverTime.text = data.formattedTime


                holder.feelingCard.setOnClickListener{
                    showReactedBottomSheet(data)
                }

                if (data.senderUid==myUid){
                    holder.messageOwnerNameTv.text="you"
                }else{
                    holder.messageOwnerNameTv.text=data.referenceMessageSenderName
                }

                if(data.referenceMessage.isNotEmpty()){
                    holder.reference_card.visibility=View.VISIBLE

                    holder.referenceMessageTv.visibility=View.VISIBLE
                    holder.refMessageLinear.visibility=View.VISIBLE
                    holder.documentImg.visibility=View.GONE

                    holder.photoLinear.visibility=View.GONE
                    holder.referenceImage.visibility=View.GONE
                    holder.refImgCard.visibility=View.GONE

                    holder.referenceMessageTv.text=data.referenceMessage

                }else if (data.referenceImgUrl.isNotEmpty()){
                    holder.reference_card.visibility=View.VISIBLE

                    holder.referenceMessageTv.visibility=View.GONE
                    holder.refMessageLinear.visibility=View.GONE
                    holder.documentImg.visibility=View.GONE

                    holder.photoLinear.visibility=View.VISIBLE
                    holder.referenceImage.visibility=View.VISIBLE
                    holder.refImgCard.visibility=View.VISIBLE
                    holder.refImgType.setImageResource(R.drawable.photo)
                    holder.refType.text="photo"
                    Glide.with(context).load(data.referenceImgUrl).placeholder(R.drawable.photo).into(holder.referenceImage)

                }else if (data.referenceVideoUrl.isNotEmpty()){
                    holder.reference_card.visibility=View.VISIBLE

                    holder.referenceMessageTv.visibility=View.GONE
                    holder.refMessageLinear.visibility=View.GONE
                    holder.documentImg.visibility=View.GONE

                    holder.photoLinear.visibility=View.VISIBLE
                    holder.referenceImage.visibility=View.VISIBLE
                    holder.refImgCard.visibility=View.VISIBLE

                    holder.refImgType.setImageResource(R.drawable.video)
                    holder.refType.text="video"
                    holder.referenceImage.loadThumbnail(data.referenceVideoUrl)

                }else if (data.referenceDocumentName.isNotEmpty()){
                    holder.reference_card.visibility=View.VISIBLE

                    holder.referenceMessageTv.text=data.referenceDocumentName
                    holder.documentImg.setImageResource(R.drawable.file)

                    holder.refMessageLinear.visibility=View.VISIBLE
                    holder.referenceMessageTv.visibility=View.VISIBLE
                    holder.documentImg.visibility=View.VISIBLE

                    holder.photoLinear.visibility=View.GONE
                    holder.referenceImage.visibility=View.GONE
                    holder.refImgCard.visibility=View.GONE


                }else if(data.referenceVoiceUrl.isNotEmpty()){
                    holder.reference_card.visibility=View.VISIBLE

                    holder.referenceMessageTv.text="Voice message"
                    holder.documentImg.setImageResource(R.drawable.baseline_keyboard_voice_24)

                    holder.refMessageLinear.visibility=View.VISIBLE
                    holder.referenceMessageTv.visibility=View.VISIBLE
                    holder.documentImg.visibility=View.VISIBLE


                    holder.photoLinear.visibility=View.GONE
                    holder.referenceImage.visibility=View.GONE
                    holder.refImgCard.visibility=View.GONE

                }else{

                    holder.referenceMessageTv.visibility=View.GONE
                    holder.refMessageLinear.visibility=View.GONE
                    holder.documentImg.visibility=View.GONE

                    holder.photoLinear.visibility=View.GONE
                    holder.referenceImage.visibility=View.GONE
                    holder.reference_card.visibility=View.GONE
                }


                holder.reference_card.setOnLongClickListener { v ->
                    longClicked.invoke(v,true,data,position)
                    true
                }

                holder.reference_card.setOnClickListener {
                    if (data.referenceMessageId.isNotEmpty()){
                        scrollToMessage(data.referenceMessageId,position)
                    }else{
                        showToast(context,"Reference id not found")
                    }
                }


//                holder.reference_card.setOnClickListener { v ->
//                    longClicked.invoke(v,false,data,position)
//                }



                holder.feelingCard.visibility = View.GONE
                holder.countTv.visibility = View.GONE
                holder.likeImg.visibility = View.GONE
                holder.heartImg.visibility = View.GONE
                holder.surpriseImg.visibility = View.GONE
                holder.happyImg.visibility = View.GONE
                holder.angryImg.visibility = View.GONE


                if (data.deletedMessagesList.contains(myUid)){
                    holder.linearReceiver.visibility=View.GONE
                }else{
                    holder.linearReceiver.visibility=View.VISIBLE
                    val count=data.like+data.heart+data.surprise+data.happy+data.angry

                    if (count>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.countTv.visibility = View.VISIBLE
                        holder.countTv.text = count.toString()
                    }

                    if (data.like>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.likeImg.visibility = View.VISIBLE
                    }

                    if (data.heart>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.heartImg.visibility = View.VISIBLE
                    }

                    if (data.surprise>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.surpriseImg.visibility = View.VISIBLE
                    }

                    if (data.happy>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.happyImg.visibility = View.VISIBLE
                    }

                    if (data.angry>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.angryImg.visibility = View.VISIBLE
                    }
                }
            }

            // for image
            is ImageSenderViewHolder -> {

                holder.feelingCard.setOnClickListener{
                    showReactedBottomSheet(data)
                }

                holder.senderTime.text = data.formattedTime
                val uri = preferencesHelper.getString(data.key, "1")

                if (uri.isNotEmpty() && uri!="1"){
                    Glide.with(context).load(uri).into(holder.senderImageView)
                }else{
                    Glide.with(context).load(data.imageUrl).into(holder.senderImageView)
                }

//                holder.senderImageView.setOnLongClickListener {
//                    showEmojiDialogOnLongClick(data,position,it)
//                    true
//                }

                holder.senderImageView.setOnLongClickListener { v ->
                    longClicked.invoke(v,true,data,position)
                    true
                }

                holder.senderImageView.setOnClickListener { v ->
                    longClicked.invoke(v,false,data,position)
                }


                holder.senderImageView.setOnClickListener {
                    val intent = Intent(context, PhotoViewActivity::class.java)
                    intent.putExtra("image", data.imageUrl)
                    context.startActivity(intent)
                }

                holder.feelingCard.visibility = View.GONE
                holder.countTv.visibility = View.GONE
                holder.likeImg.visibility = View.GONE
                holder.heartImg.visibility = View.GONE
                holder.surpriseImg.visibility = View.GONE
                holder.happyImg.visibility = View.GONE
                holder.angryImg.visibility = View.GONE

                if (data.deleteMessageFromMe){
                    holder.linearSender.visibility=View.GONE
                }else{
                    holder.linearSender.visibility=View.VISIBLE
                    val count=data.like+data.heart+data.surprise+data.happy+data.angry
                    if (count>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.countTv.visibility = View.VISIBLE
                        holder.countTv.text = count.toString()
                    }

                    if (data.like>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.likeImg.visibility = View.VISIBLE
                    }

                    if (data.heart>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.heartImg.visibility = View.VISIBLE
                    }

                    if (data.surprise>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.surpriseImg.visibility = View.VISIBLE
                    }

                    if (data.happy>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.happyImg.visibility = View.VISIBLE
                    }

                    if (data.angry>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.angryImg.visibility = View.VISIBLE
                    }
                }

            }
            is ImageReceiverViewHolder -> {

                holder.feelingCard.setOnClickListener{
                    showReactedBottomSheet(data)
                }

                Glide.with(context).load(data.imageUrl).into(holder.receiverImageView)
                holder.receiverTime.text = data.formattedTime

                holder.receiverImageView.setOnClickListener {
                    val intent = Intent(context, PhotoViewActivity::class.java)
                    intent.putExtra("image", data.imageUrl)
                    context.startActivity(intent)
                }

                holder.receiverImageView.setOnLongClickListener {
                    longClicked.invoke(it,true,data,position)
                    true
                }


                holder.feelingCard.visibility = View.GONE
                holder.countTv.visibility = View.GONE
                holder.likeImg.visibility = View.GONE
                holder.heartImg.visibility = View.GONE
                holder.surpriseImg.visibility = View.GONE
                holder.happyImg.visibility = View.GONE
                holder.angryImg.visibility = View.GONE

                if (data.deletedMessagesList.contains(myUid)){
                    holder.linearReceiver.visibility=View.GONE
                }else{
                    holder.linearReceiver.visibility=View.VISIBLE
                    val count=data.like+data.heart+data.surprise+data.happy+data.angry

                    if (count>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.countTv.visibility = View.VISIBLE
                        holder.countTv.text = count.toString()
                    }

                    if (data.like>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.likeImg.visibility = View.VISIBLE
                    }

                    if (data.heart>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.heartImg.visibility = View.VISIBLE
                    }

                    if (data.surprise>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.surpriseImg.visibility = View.VISIBLE
                    }

                    if (data.happy>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.happyImg.visibility = View.VISIBLE
                    }

                    if (data.angry>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.angryImg.visibility = View.VISIBLE
                    }
                }

            }

            // for video
            is VideoSenderViewHolder -> {

                holder.feelingCard.setOnClickListener{
                    showReactedBottomSheet(data)
                }

                holder.senderTime.text = data.formattedTime

                val uri = preferencesHelper.getString(data.key, "1")

                if (uri.isNotEmpty() && uri!="1"){
                    holder.senderVideThumbnail.loadThumbnail(uri)
                }else{
                    holder.senderVideThumbnail.loadThumbnail(data.videoUrl)
                }

                if (uri.isNotEmpty() && uri != "1") {
                    holder.playVideoImg.visibility = View.VISIBLE
                    holder.downloadFileImg.visibility = View.INVISIBLE
                    holder.circularProgressIndicator.visibility = View.INVISIBLE
                }

                holder.playVideoImg.setOnClickListener {
                    val intent = Intent(context, VideosActivity::class.java)
                    intent.putExtra("videoUrl", uri)
                    context.startActivity(intent)
                }

                holder.senderVideThumbnail.setOnLongClickListener {
                    longClicked.invoke(it,true,data,position)
                    true
                }


                holder.downloadFileImg.setOnClickListener {
                    key = data.key
                    CoroutineScope(Dispatchers.Main).launch {
                        downloadVideo(
                            context,
                            data.videoUrl,
                            "teacher_portal_video${System.currentTimeMillis()}.mp4",
                            downloadManager,
                            "Downloading Video",
                            "Downloading Video"
                        )
                    }
                    holder.downloadFileImg.visibility = View.INVISIBLE
                    holder.circularProgressIndicator.visibility = View.VISIBLE
                }

                holder.itemView.setOnClickListener {

                    if (uri.isNotEmpty() && uri != "1") {
                        val intent = Intent(context, VideosActivity::class.java)
                        intent.putExtra("videoUrl", uri)
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "download file", Toast.LENGTH_SHORT).show()
                    }

                }


                holder.feelingCard.visibility = View.GONE
                holder.countTv.visibility = View.GONE
                holder.likeImg.visibility = View.GONE
                holder.heartImg.visibility = View.GONE
                holder.surpriseImg.visibility = View.GONE
                holder.happyImg.visibility = View.GONE
                holder.angryImg.visibility = View.GONE


                if (data.deleteMessageFromMe){
                    holder.linearSender.visibility=View.GONE
                }else{
                    holder.linearSender.visibility=View.VISIBLE
                    val count=data.like+data.heart+data.surprise+data.happy+data.angry
                    if (count>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.countTv.visibility = View.VISIBLE
                        holder.countTv.text = count.toString()
                    }

                    if (data.like>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.likeImg.visibility = View.VISIBLE
                    }

                    if (data.heart>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.heartImg.visibility = View.VISIBLE
                    }

                    if (data.surprise>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.surpriseImg.visibility = View.VISIBLE
                    }

                    if (data.happy>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.happyImg.visibility = View.VISIBLE
                    }

                    if (data.angry>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.angryImg.visibility = View.VISIBLE
                    }
                }
            }
            is VideoReceiverViewHolder -> {

                holder.feelingCard.setOnClickListener{
                    showReactedBottomSheet(data)
                }
                holder.receiverThumbnailImageView.loadThumbnail(data.videoUrl)

                holder.receiverTime.text = data.formattedTime
                val uri = preferencesHelper.getString(data.key, "1")

                if (uri.isNotEmpty() && uri != "1") {
                    holder.playImg.visibility = View.VISIBLE
                    holder.downloadFileImg.visibility = View.INVISIBLE
                    holder.circularProgressIndicator.visibility = View.INVISIBLE
                }

                holder.playImg.setOnClickListener {
                    val intent = Intent(context, VideosActivity::class.java)
                    intent.putExtra("videoUrl", uri)
                    context.startActivity(intent)
                }

                holder.receiverThumbnailImageView.setOnLongClickListener {
                    longClicked.invoke(it,true,data,position)
                    true
                }


                holder.downloadFileImg.setOnClickListener {
                    key = data.key
                    CoroutineScope(Dispatchers.Main).launch {
                        downloadVideo(
                            context,
                            data.videoUrl,
                            "teacher_portal_${System.currentTimeMillis()}.mp4",
                            downloadManager,
                            "Downloading Video",
                            "Downloading Video"
                        )
                    }
                    holder.downloadFileImg.visibility = View.INVISIBLE
                    holder.circularProgressIndicator.visibility = View.VISIBLE
                }

                holder.itemView.setOnClickListener {
                    if (uri.isNotEmpty() && uri != "1") {
                        val intent = Intent(context, VideosActivity::class.java)
                        intent.putExtra("videoUrl", uri)
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "download file", Toast.LENGTH_SHORT).show()
                    }
                }


                holder.feelingCard.visibility = View.GONE
                holder.countTv.visibility = View.GONE
                holder.likeImg.visibility = View.GONE
                holder.heartImg.visibility = View.GONE
                holder.surpriseImg.visibility = View.GONE
                holder.happyImg.visibility = View.GONE
                holder.angryImg.visibility = View.GONE


                if (data.deletedMessagesList.contains(myUid)){
                    holder.linearReceiver.visibility=View.GONE
                }else{
                    holder.linearReceiver.visibility=View.VISIBLE
                    val count=data.like+data.heart+data.surprise+data.happy+data.angry

                    if (count>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.countTv.visibility = View.VISIBLE
                        holder.countTv.text = count.toString()
                    }

                    if (data.like>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.likeImg.visibility = View.VISIBLE
                    }

                    if (data.heart>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.heartImg.visibility = View.VISIBLE
                    }

                    if (data.surprise>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.surpriseImg.visibility = View.VISIBLE
                    }

                    if (data.happy>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.happyImg.visibility = View.VISIBLE
                    }

                    if (data.angry>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.angryImg.visibility = View.VISIBLE
                    }
                }

            }

            // for documents
            is DocumentSenderViewHolder -> {

                holder.feelingCard.setOnClickListener{
                    showReactedBottomSheet(data)
                }

                holder.send_time_tv.text = data.formattedTime
                holder.file_name_tv.text = data.documentFileName

                val uri = preferencesHelper.getString(data.key, "1")

                if (uri.isNotEmpty() && uri != "1") {
                    holder.downloadImg.visibility = View.GONE
                    holder.progressBar.visibility = View.GONE
                } else {
                    holder.downloadImg.visibility = View.VISIBLE
                    holder.progressBar.visibility = View.GONE
                }

                holder.downloadImg.setOnClickListener {
                    key = data.key
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.downloadImg.visibility = View.GONE
                        holder.progressBar.visibility = View.VISIBLE
                        downloadVideo(
                            context,
                            data.documentUrl,
                            data.documentFileName,
                            downloadManager,
                            "Downloading Document",
                            data.documentFileName
                        )
                    }
                }

                holder.itemView.setOnClickListener {
                    if (uri.isNotEmpty()) {
                        openDocument(Uri.parse(uri), context)
                    } else {
                        Toast.makeText(context, "download file", Toast.LENGTH_SHORT).show()
                    }
                }


                holder.feelingCard.visibility = View.GONE
                holder.countTv.visibility = View.GONE
                holder.likeImg.visibility = View.GONE
                holder.heartImg.visibility = View.GONE
                holder.surpriseImg.visibility = View.GONE
                holder.happyImg.visibility = View.GONE
                holder.angryImg.visibility = View.GONE

                if (data.deleteMessageFromMe){
                    holder.linearSender.visibility=View.GONE
                }else{
                    holder.linearSender.visibility=View.VISIBLE
                    val count=data.like+data.heart+data.surprise+data.happy+data.angry
                    if (count>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.countTv.visibility = View.VISIBLE
                        holder.countTv.text = count.toString()
                    }

                    if (data.like>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.likeImg.visibility = View.VISIBLE
                    }

                    if (data.heart>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.heartImg.visibility = View.VISIBLE
                    }

                    if (data.surprise>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.surpriseImg.visibility = View.VISIBLE
                    }

                    if (data.happy>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.happyImg.visibility = View.VISIBLE
                    }

                    if (data.angry>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.angryImg.visibility = View.VISIBLE
                    }
                }

            }
            is DocumentReceiverViewHolder -> {

                holder.feelingCard.setOnClickListener{
                    showReactedBottomSheet(data)
                }

                holder.receiver_timeTv.text = data.formattedTime
                holder.file_name_tv.text = data.documentFileName
                val uri = preferencesHelper.getString(data.key, "1")

                if (uri.isNotEmpty() && uri != "1") {
                    holder.downloadImg.visibility = View.GONE
                    holder.progressBar.visibility = View.GONE
                } else {
                    holder.downloadImg.visibility = View.VISIBLE
                    holder.progressBar.visibility = View.GONE
                }

                holder.downloadImg.setOnClickListener {
                    key = data.key
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.downloadImg.visibility = View.GONE
                        holder.progressBar.visibility = View.VISIBLE
                        downloadVideo(
                            context,
                            data.documentUrl,
                            data.documentFileName,
                            downloadManager,
                            "Downloading Document",
                            data.documentFileName
                        )
                    }
                }

                holder.itemView.setOnClickListener {
                    if (uri.isNotEmpty()) {
                        openDocument(Uri.parse(uri), context)
                    } else {
                        Toast.makeText(context, "download file", Toast.LENGTH_SHORT).show()
                    }
                }


                holder.feelingCard.visibility = View.GONE
                holder.countTv.visibility = View.GONE
                holder.likeImg.visibility = View.GONE
                holder.heartImg.visibility = View.GONE
                holder.surpriseImg.visibility = View.GONE
                holder.happyImg.visibility = View.GONE
                holder.angryImg.visibility = View.GONE

                if (data.deletedMessagesList.contains(myUid)){
                    holder.linearReceiver.visibility=View.GONE
                }else{
                    holder.linearReceiver.visibility=View.VISIBLE
                    val count=data.like+data.heart+data.surprise+data.happy+data.angry

                    if (count>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.countTv.visibility = View.VISIBLE
                        holder.countTv.text = count.toString()
                    }

                    if (data.like>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.likeImg.visibility = View.VISIBLE
                    }

                    if (data.heart>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.heartImg.visibility = View.VISIBLE
                    }

                    if (data.surprise>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.surpriseImg.visibility = View.VISIBLE
                    }

                    if (data.happy>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.happyImg.visibility = View.VISIBLE
                    }

                    if (data.angry>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.angryImg.visibility = View.VISIBLE
                    }
                }

            }

            // for sender voice.
            is VoiceSenderViewHolder -> {

                val localUri = preferencesHelper.getString(data.key,"")

                holder.seekBar.setOnLongClickListener { v ->
                    longClicked.invoke(v,true,data,position)
                    true
                }

                holder.feelingCard.setOnClickListener{
                    showReactedBottomSheet(data)
                }

                scope.launch(Dispatchers.Main) {
                    if (localUri.isEmpty()) {
                        holder.playButton.loadImageFromResource(R.drawable.baseline_file_download_24)
                        val path=withContext(Dispatchers.IO){context.downloadAudio(data.voiceUrl)}
                        preferencesHelper.saveString(data.key,path)
                        notifyDataSetChanged()
                    } else {
                        holder.playButton.loadImageFromResource(R.drawable.baseline_play_arrow_24)
                    }
                }


                holder.apply {

                    voiceSendTime.text = data.formattedTime
                    seekBar.max = 100

                    playButton.setOnClickListener {
                        if (localUri.isNotEmpty()) {
                            if (currentlyPlayingMediaPlayer?.isPlaying == true) {

                                if (currentPlayingButton == playButton) {

                                    currentlyPlayingMediaPlayer?.let {
                                        data.currentAudioPosition = it.currentPosition
                                        it.pause()
                                        playButton.setImageResource(R.drawable.baseline_play_arrow_24)
                                    }

                                } else {

                                    stopCurrentMediaPlayer(data)

                                    currentlyPlayingMediaPlayer = getMediaPlayerInstance().apply {
                                        reset()
                                        setDataSource(localUri)
                                        prepare()
                                        seekTo(data.currentAudioPosition)
                                        start()
                                    }

                                    currentPlayingButton = playButton
                                    currentSeekBar = seekBar

                                    currentSeekBar.progress = (100 * data.currentAudioPosition / currentlyPlayingMediaPlayer?.duration!!)
                                    playButton.setImageResource(R.drawable.baseline_pause_24)

                                    // Update seek bar
                                    handler.postDelayed(object : Runnable {
                                        override fun run() {
                                            currentlyPlayingMediaPlayer?.let {
                                                currentSeekBar.progress = (100 * it.currentPosition / it.duration)
                                                data.currentAudioPosition = it.currentPosition
                                                if (it.isPlaying) {
                                                    handler.postDelayed(this, 100)
                                                }
                                            }
                                        }
                                    }, 0)
                                }
                            } else {

                                currentlyPlayingMediaPlayer = getMediaPlayerInstance().apply {
                                    reset()
                                    setDataSource(localUri)
                                    prepare()
                                    seekTo(data.currentAudioPosition)
                                    start()
                                }

                                currentPlayingButton = playButton
                                currentSeekBar = seekBar

                                currentSeekBar.progress = (100 * data.currentAudioPosition / currentlyPlayingMediaPlayer?.duration!!)
                                playButton.setImageResource(R.drawable.baseline_pause_24)

                                // Update seek bar
                                handler.postDelayed(object : Runnable {
                                    override fun run() {
                                        currentlyPlayingMediaPlayer?.let {
                                            currentSeekBar.progress = (100 * it.currentPosition / it.duration)
                                            data.currentAudioPosition = it.currentPosition
                                            if (it.isPlaying) {
                                                handler.postDelayed(this, 100)
                                            }
                                        }
                                    }
                                }, 0)
                            }

                            currentlyPlayingMediaPlayer?.setOnCompletionListener {
                                data.currentAudioPosition = 0
                                stopCurrentMediaPlayer(data, true)
                            }
                        } else {
                            showToast(context, "URI not found")
                        }
                    }

                    seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                            if (fromUser) {
                                currentlyPlayingMediaPlayer?.let {
                                    val playPosition = (it.duration * progress) / 100
                                    it.seekTo(playPosition)
                                }
                            }
                        }
                        override fun onStartTrackingTouch(seekBar: SeekBar?) {
                            // Optional: Add any additional logic when the user starts to move the seek bar
                        }
                        override fun onStopTrackingTouch(seekBar: SeekBar?) {
                            // Optional: Add any additional logic when the user stops moving the seek bar
                        }
                    })



                    holder.feelingCard.visibility = View.GONE
                    holder.countTv.visibility = View.GONE
                    holder.likeImg.visibility = View.GONE
                    holder.heartImg.visibility = View.GONE
                    holder.surpriseImg.visibility = View.GONE
                    holder.happyImg.visibility = View.GONE
                    holder.angryImg.visibility = View.GONE


                    if (data.deleteMessageFromMe){
                        holder.linearSender.visibility=View.GONE
                    }else{
                        holder.linearSender.visibility=View.VISIBLE
                        val count=data.like+data.heart+data.surprise+data.happy+data.angry
                        if (count>0){
                            holder.feelingCard.visibility = View.VISIBLE
                            holder.countTv.visibility = View.VISIBLE
                            holder.countTv.text = count.toString()
                        }

                        if (data.like>0){
                            holder.feelingCard.visibility = View.VISIBLE
                            holder.likeImg.visibility = View.VISIBLE
                        }

                        if (data.heart>0){
                            holder.feelingCard.visibility = View.VISIBLE
                            holder.heartImg.visibility = View.VISIBLE
                        }

                        if (data.surprise>0){
                            holder.feelingCard.visibility = View.VISIBLE
                            holder.surpriseImg.visibility = View.VISIBLE
                        }

                        if (data.happy>0){
                            holder.feelingCard.visibility = View.VISIBLE
                            holder.happyImg.visibility = View.VISIBLE
                        }

                        if (data.angry>0){
                            holder.feelingCard.visibility = View.VISIBLE
                            holder.angryImg.visibility = View.VISIBLE
                        }
                    }

                }

            }
            is VoiceReceiverViewHolder -> {

                val localUri = preferencesHelper.getString(data.key,"")

                holder.voiceSendTime.text =data.formattedTime

                holder.seekBar.setOnLongClickListener { v ->
                    longClicked.invoke(v,true,data,position)
                    true
                }

                holder.feelingCard.setOnClickListener{
                    showReactedBottomSheet(data)
                }

                scope.launch(Dispatchers.Main){

                    if (localUri.isEmpty()) {
                        holder.playButton.loadImageFromResource(R.drawable.baseline_file_download_24)
                        val path=withContext(Dispatchers.IO){context.downloadAudio(data.voiceUrl)}
                        preferencesHelper.saveString(data.key,path)
                        notifyDataSetChanged()
                    } else {
                        holder.playButton.loadImageFromResource(R.drawable.baseline_play_arrow_24)
                    }

                    holder.apply {
                        playButton.setOnClickListener {
                            if (localUri.isNotEmpty()) {
                                if (currentlyPlayingMediaPlayer?.isPlaying == true) {

                                    if (currentPlayingButton == playButton) {

                                        currentlyPlayingMediaPlayer?.let {
                                            data.currentAudioPosition = it.currentPosition
                                            it.pause()
                                            playButton.setImageResource(R.drawable.baseline_play_arrow_24)
                                        }

                                    } else {

                                        stopCurrentMediaPlayer(data)

                                        currentlyPlayingMediaPlayer = getMediaPlayerInstance().apply {
                                            reset()
                                            setDataSource(localUri)
                                            prepare()
                                            seekTo(data.currentAudioPosition)
                                            start()
                                        }

                                        currentPlayingButton = playButton
                                        currentSeekBar = seekBar

                                        currentSeekBar.progress = (100 * data.currentAudioPosition / currentlyPlayingMediaPlayer?.duration!!)
                                        playButton.setImageResource(R.drawable.baseline_pause_24)

                                        // Update seek bar
                                        handler.postDelayed(object : Runnable {
                                            override fun run() {
                                                currentlyPlayingMediaPlayer?.let {
                                                    currentSeekBar.progress = (100 * it.currentPosition / it.duration)
                                                    data.currentAudioPosition = it.currentPosition
                                                    if (it.isPlaying) {
                                                        handler.postDelayed(this, 100)
                                                    }
                                                }
                                            }
                                        }, 0)
                                    }



                                } else {

                                    currentlyPlayingMediaPlayer = getMediaPlayerInstance().apply {
                                        reset()
                                        setDataSource(localUri)
                                        prepare()
                                        seekTo(data.currentAudioPosition)
                                        start()
                                    }

                                    currentPlayingButton = playButton
                                    currentSeekBar = seekBar

                                    currentSeekBar.progress = (100 * data.currentAudioPosition / currentlyPlayingMediaPlayer?.duration!!)
                                    playButton.setImageResource(R.drawable.baseline_pause_24)

                                    // Update seek bar
                                    handler.postDelayed(object : Runnable {
                                        override fun run() {
                                            currentlyPlayingMediaPlayer?.let {
                                                currentSeekBar.progress = (100 * it.currentPosition / it.duration)
                                                data.currentAudioPosition = it.currentPosition
                                                if (it.isPlaying) {
                                                    handler.postDelayed(this, 100)
                                                }
                                            }
                                        }
                                    }, 0)
                                }

                                currentlyPlayingMediaPlayer?.setOnCompletionListener {
                                    data.currentAudioPosition = 0
                                    stopCurrentMediaPlayer(data, true)
                                }
                            } else {
                                showToast(context, "URI not found")
                            }
                        }
                    }
                }




                holder.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                        if (fromUser) {
                            currentlyPlayingMediaPlayer?.let {
                                val playPosition = (it.duration * progress) / 100
                                it.seekTo(playPosition)
                            }
                        }
                    }
                    override fun onStartTrackingTouch(seekBar: SeekBar?) {
                        // Optional: Add any additional logic when the user starts to move the seek bar
                    }
                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        // Optional: Add any additional logic when the user stops moving the seek bar
                    }
                })



                holder.feelingCard.visibility = View.GONE
                holder.countTv.visibility = View.GONE
                holder.likeImg.visibility = View.GONE
                holder.heartImg.visibility = View.GONE
                holder.surpriseImg.visibility = View.GONE
                holder.happyImg.visibility = View.GONE
                holder.angryImg.visibility = View.GONE


                if (data.deletedMessagesList.contains(myUid)){
                    holder.linearReceiver.visibility=View.GONE
                }else{
                    holder.linearReceiver.visibility=View.VISIBLE
                    val count=data.like+data.heart+data.surprise+data.happy+data.angry

                    if (count>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.countTv.visibility = View.VISIBLE
                        holder.countTv.text = count.toString()
                    }

                    if (data.like>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.likeImg.visibility = View.VISIBLE
                    }

                    if (data.heart>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.heartImg.visibility = View.VISIBLE
                    }

                    if (data.surprise>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.surpriseImg.visibility = View.VISIBLE
                    }

                    if (data.happy>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.happyImg.visibility = View.VISIBLE
                    }

                    if (data.angry>0){
                        holder.feelingCard.visibility = View.VISIBLE
                        holder.angryImg.visibility = View.VISIBLE
                    }
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return mylist.size
    }

    override fun getItemViewType(position: Int): Int {
        val data = mylist[position]

        val isSender = data.senderUid == myUid

        return when {
            isSender -> {
                when {
                    // sender text
                    data.message.isNotEmpty() -> SENDER_VIEW_SIMPLE_MESSAGE
                    data.voiceUrl.isNotEmpty() -> SENDER_VOICE_MESSAGE
                    data.imageUrl.isNotEmpty() -> SENDER_IMAGE_MESSAGE
                    data.videoUrl.isNotEmpty() -> SENDER_VIDEO_MESSAGE
                    data.documentUrl.isNotEmpty() -> SENDER_DOCUMENT_MESSAGE
                    else -> 0
                }
            }

            else -> {
                when {
                    // receiver text
                    data.message.isNotEmpty() -> RECEIVER_VIEW_SIMPLE_MESSAGE

                    data.voiceUrl.isNotEmpty() -> RECEIVER_VOICE_MESSAGE
                    data.imageUrl.isNotEmpty() -> RECEIVER_IMAGE_MESSAGE
                    data.videoUrl.isNotEmpty() -> RECEIVER_VIDEO_MESSAGE
                    data.documentUrl.isNotEmpty() -> RECEIVER_DOCUMENT_MESSAGE

                    else -> 0
                }
            }
        }
    }



    companion object {
        var key = ""

        @SuppressLint("StaticFieldLeak")
        var adapte: ChatAdapter? = null
        fun videoUri(uri: String, context: Context) {
            val preferencesHelper = SharedPreferencesHelper(context)
            preferencesHelper.saveString(key, uri)
            refreshAdapter()
        }

        fun setAdapter(chatAdapter: ChatAdapter) {
            adapte = chatAdapter
        }

        @SuppressLint("NotifyDataSetChanged")
        fun refreshAdapter() {
            adapte?.notifyDataSetChanged()
        }
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

    @SuppressLint("NotifyDataSetChanged")
    fun setList(serverList: List<MessageModel>){
        this.mylist=serverList
        notifyDataSetChanged()
    }


    fun showReactedBottomSheet(data:MessageModel){

        val previousReactionPath="$chatPath/${data.key}"
        val reactionDetailsPath="reactionsDetails/${getSortedKeys(userModel.key,auth.currentUser!!.uid)}/${data.key}"

        val bottomSheetDialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.reaction_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)
        val recyclerView=view.findViewById<RecyclerView>(R.id.recyclerView)
        bottomSheetDialog.show()

        scope.launch {
            mainViewModel.collectAnyModel(reactionDetailsPath , ReactionModel::class.java).collect{
                for (i in it){
                    val model=mainViewModel.getAnyData("$USERS/${i.senderKey}",UserModel::class.java)!!
                    i.senderName=model.fullName
                    i.senderImageUrl=model.profileUrl
                }

                val sortedList = it.sortedByDescending { it.senderKey == myUid}

                recyclerView.setData(sortedList, ReactionSampleRowBinding::inflate){ binding, item, position ->

                    binding.userImg.loadImageViaLink(item.senderImageUrl)
                    binding.userName.text=item.senderName

                    if (item.senderKey==myUid){
                        binding.tabToRemove.visibility=View.VISIBLE
                    }else{
                        binding.tabToRemove.visibility=View.GONE
                    }

                    binding.userLinearLayout.setOnClickListener {

                        if (item.senderKey==myUid){
                            scope.launch {
                                val reaction=getReaction(item.reactionId)
                                var previousCount=0

                                when(reaction){
                                    "like"->{
                                        previousCount=data.like-1
                                    }
                                    "heart"->{
                                        previousCount=data.heart-1
                                    }
                                    "surprise"->{
                                        previousCount=data.surprise-1
                                    }
                                    "happy"->{
                                        previousCount=data.happy-1
                                    }
                                    "angry"->{
                                        previousCount=data.angry-1
                                    }
                                }

                                val map=HashMap<String,Any>()
                                map[reaction]=previousCount

                                showToast(context,"removed")
                                bottomSheetDialog.dismiss()

                                databaseReference.child("$reactionDetailsPath/$myUid").removeValue().await()
                                databaseReference.child(previousReactionPath).updateChildren(map).await()
                            }
                        }

                    }

                    when(item.reactionId){
                        1->{
                            binding.reactionImg.setImageResource(R.drawable.ic_like)
                        }
                        2->{
                            binding.reactionImg.setImageResource(R.drawable.ic_love)
                        }
                        3->{
                            binding.reactionImg.setImageResource(R.drawable.surprise_img)
                        }
                        4->{
                            binding.reactionImg.setImageResource(R.drawable.ic_laugh)
                        }
                        5->{
                            binding.reactionImg.setImageResource(R.drawable.ic_angry)
                        }
                    }

                }
            }

        }
    }


    // scroll to reference message
    fun scrollToMessage(referenceKey: String, currentItemPosition: Int) {
        val position = mylist.indexOfFirst { it.key == referenceKey }
        Log.i("TAG", "scrollToMessage:$position")
        if (position != -1) {
            layoutManager.smoothScrollToPosition(recyclerView, RecyclerView.State(), position)
            highlightMessage(position)
        }else{
            Log.i("TAG", "scrollToMessage:$position")
        }
    }

    private fun highlightMessage(position: Int) {
        recyclerView.post {
            val viewHolder = recyclerView.findViewHolderForAdapterPosition(position)
            if (viewHolder != null && viewHolder.itemView.isAttachedToWindow) {
                val backgroundColor = ContextCompat.getColor(context, R.color.very_light_grey)
                val animator = ObjectAnimator.ofArgb(viewHolder.itemView, "backgroundColor", Color.TRANSPARENT, backgroundColor)
                animator.duration = 1000
                animator.interpolator = AccelerateDecelerateInterpolator()
                animator.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        // Do something if needed
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        val reverseAnimator = ObjectAnimator.ofArgb(viewHolder.itemView, "backgroundColor", backgroundColor, Color.TRANSPARENT)
                        reverseAnimator.duration = 1000
                        reverseAnimator.interpolator = AccelerateDecelerateInterpolator()
                        reverseAnimator.start()
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        // Do something if needed
                    }

                    override fun onAnimationRepeat(animation: Animator) {
                        // Do something if needed
                    }
                })
                animator.start()
            }
        }
    }



    private fun stopCurrentMediaPlayer(data: MessageModel,isCompleted:Boolean=false) {

        if (isCompleted){
            currentSeekBar.progress=0
            data.currentAudioPosition=0
        }

        currentlyPlayingMediaPlayer?.pause()
        currentlyPlayingMediaPlayer?.release()

        currentlyPlayingMediaPlayer = null
        currentPlayingButton?.setImageResource(R.drawable.baseline_play_arrow_24)

        handler.removeCallbacksAndMessages(null)

    }

    private fun getMediaPlayerInstance(): MediaPlayer {
        return currentlyPlayingMediaPlayer ?: MediaPlayer()
    }



}