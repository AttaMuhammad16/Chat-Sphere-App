package com.atta.chatspherapp.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.CountDownTimer
import android.os.Environment
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.atta.chatspherapp.R
import com.atta.chatspherapp.ui.activities.room.EditImageActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.storage
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


object NewUtils {


    private val _flow = MutableStateFlow(0)
    val flow: StateFlow<Int>
        get() = _flow

    // Function to update the flow value
    fun updateFlow(value: Int) {
        _flow.value = value
    }

    fun Spinner.getSelectedText(): String {
        return selectedItem.toString()
    }

    fun showMessageDeleteDialog(context: Activity,isSenderOrReceiver:Boolean=true,callBack: (Int) -> Unit){
        CoroutineScope(Dispatchers.Main).launch{
            val dialog = AlertDialog.Builder(context).setView(R.layout.message_delete_dialog).show()

            val deleteEveryOne=dialog.findViewById<TextView>(R.id.deleteEveryOne)
            val deleteForMe=dialog.findViewById<TextView>(R.id.deleteForMe)
            val cancel=dialog.findViewById<TextView>(R.id.cancel)

            if (!isSenderOrReceiver){
                deleteEveryOne.visibility=View.GONE
            }

            deleteEveryOne.setOnClickListener {
                callBack.invoke(1)
                dialog.dismiss()
            }

            deleteForMe.setOnClickListener {
                callBack.invoke(2)
                dialog.dismiss()
            }

            cancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    fun View.zoomIn(duration: Long) {
        val scaleX = ObjectAnimator.ofFloat(this, View.SCALE_X, 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 0f, 1f)
        scaleX.duration = duration
        scaleY.duration = duration
        scaleX.start()
        scaleY.start()
    }

    fun View.zoomOut(duration: Long = 500) {
        val scaleX = ObjectAnimator.ofFloat(this, View.SCALE_X, 1f, 0f)
        val scaleY = ObjectAnimator.ofFloat(this, View.SCALE_Y, 1f, 0f)
        scaleX.duration = duration
        scaleY.duration = duration
        scaleX.start()
        scaleY.start()
    }


    fun showReactionDialog(v:View,context: Activity,callBack:(Int)->Unit){

        val location = IntArray(2)

        v.getLocationOnScreen(location)
        val yPosition = location[1]

        val dialog = AlertDialog.Builder(context).setView(R.layout.reaction_dialog).show()

        val width = (context.resources.displayMetrics.widthPixels * 0.6).toInt()
        dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)

        val like_anim=dialog.findViewById<LottieAnimationView>(R.id.like_anim)
        val heart_anim=dialog.findViewById<LottieAnimationView>(R.id.heart_anim)
        val surprise_anim=dialog.findViewById<LottieAnimationView>(R.id.surprise_anim)
        val happy_anim=dialog.findViewById<LottieAnimationView>(R.id.happy_anim)
        val angry_anim=dialog.findViewById<LottieAnimationView>(R.id.angry_anim)

        val copyImg=dialog.findViewById<ImageView>(R.id.copyImg)
        val deleteImg=dialog.findViewById<ImageView>(R.id.deleteImg)


        like_anim.setOnClickListener {
            callBack.invoke(1)
            dialog.dismiss()
        }

        heart_anim.setOnClickListener {
            callBack.invoke(2)
            dialog.dismiss()
        }

        surprise_anim.setOnClickListener {
            callBack.invoke(3)
            dialog.dismiss()
        }

        happy_anim.setOnClickListener {
            callBack.invoke(4)
            dialog.dismiss()
        }

        angry_anim.setOnClickListener {
            callBack.invoke(5)
            dialog.dismiss()
        }

        copyImg.setOnClickListener {
            callBack.invoke(6)
            dialog.dismiss()
        }

        deleteImg.setOnClickListener {
            callBack.invoke(7)
            dialog.dismiss()
        }


        val animationDuration:Long=800
        like_anim.zoomIn(animationDuration)
        heart_anim.zoomIn(animationDuration)
        surprise_anim.zoomIn(animationDuration)
        happy_anim.zoomIn(animationDuration)
        angry_anim.zoomIn(animationDuration)

        val layoutParams = dialog.window?.attributes
        layoutParams?.gravity = Gravity.TOP
        layoutParams?.y = yPosition - 100
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

    }



    @SuppressLint("QueryPermissionsNeeded")
    fun openDocument(uri: Uri, context: Context) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = uri
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
        context.startActivity(intent)
    }

    suspend fun downloadVideo(context: Context, videoUrl: String,fileNameWithExtension:String,downloadManager:DownloadManager): Long {
        val request = DownloadManager.Request(Uri.parse(videoUrl))
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        request.setTitle("Downloading Video")
        request.setDescription("Downloading video")
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileNameWithExtension)
        return downloadManager.enqueue(request)
    }


    fun getFileName(uri: Uri,context: Activity): String {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return try {
            if (cursor != null && cursor.moveToFirst()) {
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                cursor.getString(displayNameIndex)
            } else {
                "UnknownFileName_${System.currentTimeMillis()}"
            }
        } finally {
            cursor?.close()
        }
    }

    fun gotoEditActivity(context:Activity,contentUri:Uri,key:String="DATA",requestCode: Int){
        val intent = Intent(context, EditImageActivity::class.java)
        intent.putExtra(key, contentUri.toString())
        context.startActivityForResult(intent, requestCode)
    }

    fun animateViewFromBottom(view: View, duration: Long = 500L) {
        view.translationY = view.height.toFloat()
        view.animate().translationY(0f).setDuration(duration).setListener(null).start()
    }

//    fun animateViewHideToBottom(view: View, duration: Long = 500L) {
//        view.animate().translationY(-view.height.toFloat()).setDuration(duration).setListener(null).start()
//    }
//

    fun TextView.deleteLastCharacter() {
        // Check if the text is not empty and can be cast to Editable
        if (this.text.isNotEmpty() && this.text is Editable) {
            // Cast the text to Editable
            val editableText = this.text as Editable
            // Delete the last character
            if (editableText.isNotEmpty()) {
                editableText.delete(editableText.length - 1, editableText.length)
            }
        }
    }
    interface SelectionListener{
        fun onSelected(text:Int)
    }

    fun convertViewToPdf(context: Context, view: View, pdfFileName: String): String? {
        val pdfDocument = PdfDocument()

        val width = view.width
        val height = view.height

        val pageInfo = PdfDocument.PageInfo.Builder(width, height, 1).create()
        val page = pdfDocument.startPage(pageInfo)

        val canvas = page.canvas
        view.draw(canvas)

        pdfDocument.finishPage(page)

        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), pdfFileName)
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        pdfDocument.close()
//        val uri = addPdfToMediaStore(context, file.absolutePath, pdfFileName)
        val uri = file.toUri()
        return uri.toString()
        }

    fun showDatePickerDialog(context: Context, view: View, onDateSelected: (Boolean) -> Unit) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)

        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            context,
            R.style.MyDatePickerDialogStyle,
            DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }

                val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                val formattedDate = sdf.format(selectedDate.time)

                // Call the onDateSelected callback to indicate that a date has been selected
                onDateSelected(true)

                // Update the view's text based on its type
                when (view) {
                    is TextView -> view.text = formattedDate
                    is EditText -> view.setText(formattedDate)
                    is TextInputEditText -> view.setText(formattedDate)
                }
            },
            year, month, day
        )

        datePickerDialog.setTitle("Select Date")

        datePickerDialog.setOnCancelListener {
            // Call the onDateSelected callback to indicate that date selection was cancelled
            onDateSelected(false)
        }

        datePickerDialog.show()
    }



    @SuppressLint("ResourceType")
    fun AppCompatActivity.setupIconTabLayout(
        tabLayout: TabLayout,
        viewPager2: ViewPager2,
        tabIconList: List<Int>, // Resource IDs of icons
        tabTextList: List<String>,
        fragments: List<Fragment>
    ) {
        require(tabIconList.size == tabTextList.size) { "Icon and Text lists must have the same size" }

        viewPager2.adapter = object : androidx.viewpager2.adapter.FragmentStateAdapter(this) {
            override fun getItemCount(): Int = fragments.size

            override fun createFragment(position: Int): Fragment {
                return fragments[position]
            }
        }

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
             var linearLayout = LinearLayout(this)
            linearLayout.orientation = LinearLayout.HORIZONTAL
            val linearLayoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            linearLayoutParams.gravity = Gravity.CENTER
            linearLayout.layoutParams = linearLayoutParams

            val imageView = ImageView(this)
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.width = 60
            layoutParams.height = 60
            imageView.layoutParams = layoutParams

            val textView = TextView(this)
            textView.textSize = 15f
            textView.gravity = Gravity.CENTER
            textView.setTypeface(null, Typeface.BOLD)

            linearLayout.addView(imageView)
            linearLayout.addView(textView)

            imageView.setImageResource(tabIconList[position])
            textView.text = tabTextList[position]

            viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    for (i in 0 until tabLayout.tabCount) {
                        val linear = tabLayout.getTabAt(i)?.customView as LinearLayout
                        if (i == position) {
                            linear.getChildAt(0).visibility = View.GONE
                            linear.getChildAt(1).visibility = View.VISIBLE
                        } else {
                            linear.getChildAt(0).visibility = View.VISIBLE
                            linear.getChildAt(1).visibility = View.GONE
                        }
                    }
                }
            })

            tab.customView = linearLayout
        }.attach()
    }


    fun Long.toDate(pattern: String = "dd-MM-yyyy"): Date {
        return Date(this).apply {
            val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
            dateFormat.format(this)
        }
    }



   fun  RecyclerView.Adapter<RecyclerView.ViewHolder>.showToast(context: Context , message:String){
       CoroutineScope(Dispatchers.Main).launch {
             Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
       }

   }

    suspend fun uploadVideoToFirebaseStorage(
        videoUri: Uri,
        progressCallBack: (Int) -> Unit,
        uploadtask:(UploadTask)->Unit
    ): MyResult<String> {
        return try {
            val storageReference = Firebase.storage.reference
            val uploadTask = storageReference.child("${System.currentTimeMillis()}.mp4").putFile(videoUri)
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                progressCallBack(progress)
            }
            uploadtask.invoke(uploadTask)
            val result = uploadTask.await()
            val downloadUrl = result.storage.downloadUrl.await()
            MyResult.Success(downloadUrl.toString())
        } catch (e: Exception) {
            MyResult.Error(e.message ?: "Unknown error occurred")
        }
    }


    suspend fun uploadAudioToFirebase(uri: Uri): MyResult<String> {
        val fileName = "downloaded_audio.3gp"
        return try {
            val storageRef = Firebase.storage.reference.child("audios/$fileName")
            storageRef.putFile(uri).await()
            MyResult.Success(storageRef.downloadUrl.await().toString())
        }catch (e:Exception){
            MyResult.Error("")
        }
    }

    fun Context.getUriOfTheFile(filePath: String):Uri{
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(this, "${applicationContext.packageName}.fileprovider", file)
        return uri
    }

    suspend fun Context.downloadAudio(url: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val downloadPath = "${filesDir?.absolutePath}/${System.currentTimeMillis()}.3gp"
                val storageRef = com.google.firebase.ktx.Firebase.storage.getReferenceFromUrl(url)
                val file = File(downloadPath)
                storageRef.getFile(file).addOnSuccessListener {
                    Log.d("DownloadAudio", "File downloaded successfully to $downloadPath")
                }.addOnFailureListener { exception ->
                    Log.e("DownloadAudio", "Failed to download file: $exception")
                }.await()
                downloadPath
            } catch (e: Exception) {
                Log.e("DownloadAudio", "Exception occurred during file download: $e")
                ""
            }
        }
    }




    fun pickImageFromGallery(requestCode: Int,context: Activity){
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        context.startActivityForResult(intent, requestCode)
    }


    @SuppressLint("ClickableViewAccessibility")
    fun TextInputEditText.setOptions(options: List<String>) {
        var currentIndex = 0
        var startX = 0f
        var startY = 0f
        val distance = 10.0
        setText(options[currentIndex])
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    true
                }

                MotionEvent.ACTION_UP -> {
                    val endX = event.x
                    val endY = event.y
                    val deltaX = endX - startX
                    val deltaY = endY - startY
                    if (deltaX < distance && deltaY < distance) {
                        currentIndex = (currentIndex + 1) % options.size
                        setText(options[currentIndex])
                    }
                    true
                }
                else -> false
            }
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    fun MaterialAutoCompleteTextView.setOptions(options: List<String>) {
        var currentIndex = 0
        var startX = 0f
        var startY = 0f
        val distance = 10.0
        setText(options[currentIndex])
        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = event.x
                    startY = event.y
                    true
                }

                MotionEvent.ACTION_UP -> {
                    val endX = event.x
                    val endY = event.y
                    val deltaX = endX - startX
                    val deltaY = endY - startY
                    if (deltaX < distance && deltaY < distance) {
                        currentIndex = (currentIndex + 1) % options.size
                        setText(options[currentIndex])
                    }
                    true
                }
                else -> false
            }
        }
    }



    fun checkEditTexts(context: Activity, list: ArrayList<EditText>): Boolean {
        list.forEach {
            if (it.text.toString().isEmpty()) {
                context.showToast("${it.hint}")
                return false
            }
        }
        return true
    }


    fun startCountdownTimer(
        millisInFuture: Long,
        view: TextView,
        context: Activity,
        resendTv: TextView
    ): CountDownTimer {
        return object : CountDownTimer(millisInFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                with(view) {
                    text = String.format("%02d:%02d", minutes, remainingSeconds)
                }
                with(resendTv) {
                    isClickable = false
                    setTextColor(ContextCompat.getColor(context, R.color.black))
                }
            }

            override fun onFinish() {
                with(view) {
                    text = "00:00"
                }
                with(resendTv) {
                    isClickable = true
                    setTextColor(ContextCompat.getColor(context, R.color.red))
                }
            }
        }.start()
    }



    fun EditText.showSoftKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    fun EditText.hideSoftKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }




    fun Activity.launchActivity(clazz:Class<*> , key:String = "" , data:String = ""){
       var intent = Intent(this , clazz)
        if (key.isNotEmpty() && data.isNotEmpty()){
               intent.putExtra(key,data)
        }
        startActivity(intent)
    }


    fun Activity.systemBottomNavigationColor(context: Context, color: Int= R.color.cement) {
        this.window.navigationBarColor = ContextCompat.getColor(context, color)
    }

    fun Long.getFormattedDateAndTime(pattern:String = "hh:mm a"): String {
        return try {
            // Assuming currentTimeMillis is a string representation of a Long

            val dateFormat = SimpleDateFormat(pattern, Locale.getDefault())
            val date = Date(this)
            dateFormat.format(date)
        } catch (e: NumberFormatException) {
            "Invalid timestamp" // or handle the error in another way
        }
    }

    fun processPhoneNumber(phoneNumber: String): String {
        val cleanedNumber = phoneNumber.replace("\\s".toRegex(), "")
        return if (phoneNumber.startsWith("03")) {
            cleanedNumber.replaceFirst("0","")
        } else {
            cleanedNumber
        }
    }




    fun showWithRevealAnimation(showView: View, hideView: View) {
        val centerX = (showView.left + showView.right) / 2
        val centerY = (showView.top + showView.bottom) / 2

        val finalRadius = kotlin.math.hypot(showView.width.toDouble(), showView.height.toDouble()).toFloat()
        val circularReveal = ViewAnimationUtils.createCircularReveal(showView, centerX, centerY, 0f, finalRadius)

        circularReveal.duration = 700

        circularReveal.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                showView.visibility = View.VISIBLE
                hideView.visibility = View.INVISIBLE
            }
        })
        circularReveal.start()
    }



    fun hideWithRevealAnimation(hideView: View, showView: View){
        val centerX = (hideView.left + hideView.right) / 2
        val centerY = (hideView.top + hideView.bottom) / 2
        val initialRadius = Math.hypot(hideView.width.toDouble(), hideView.height.toDouble()).toFloat()
        val circularReveal = ViewAnimationUtils.createCircularReveal(hideView, centerX, centerY, initialRadius, 0f)
        circularReveal.duration = 700
        circularReveal.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                hideView.visibility = View.INVISIBLE
                showView.visibility = View.VISIBLE
            }
        })
        circularReveal.start()
    }

    fun showKeyBoard(context: Activity,view: View){
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    fun hideKeyboard(context: Activity,view: View){
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }




    fun View.animateFromDown(duration: Long = 300) {
        val slideUpAnimation = TranslateAnimation(
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.0f
        )
        slideUpAnimation.duration = duration
        this.startAnimation(slideUpAnimation)
    }


    fun animateViewHideToBottom(view: View, duration: Long = 500L) {
        view.animate().translationY(-view.height.toFloat()).setDuration(duration).setListener(null).start()
    }


    fun Context.showToast(message: String , duration:Int = Toast.LENGTH_SHORT) {
        val v = this
        CoroutineScope(Dispatchers.Main).launch {
            Toast.makeText(v, message, duration).show()
        }
    }

    fun showProgressDialog(context: Context, message: String): Dialog {
        val progressDialog = Dialog(context)
        progressDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        progressDialog.setCancelable(false)

        val view = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val messageTextView = view.findViewById<TextView>(R.id.messageTextView)
        messageTextView.text = message
        progressDialog.setContentView(view)
        progressDialog.show()
        return progressDialog
    }

    fun Activity.setStatusBarColor(color: Int) {
        window.statusBarColor = ContextCompat.getColor(this,color)
    }


    // paging adapter
    inline fun <T, VB : ViewBinding> RecyclerView.setData(items: List<T>, crossinline bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> VB, crossinline bindHolder: (binding: VB, item: T, position: Int) -> Unit) {
        val adapter = object : RecyclerView.Adapter<DataViewHolder<VB>>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder<VB> {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = bindingInflater(layoutInflater, parent, false)
                return DataViewHolder(binding)
            }
            override fun onBindViewHolder(holder: DataViewHolder<VB>, position: Int) {
                bindHolder(holder.binding, items[position], position)
            }
            override fun getItemCount(): Int {
                return  items.size
            }
        }
        this.adapter = adapter
    }

    class DataViewHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)




    fun EditText.onTextChange(callback: (String) -> Unit){
        this.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                callback(s.toString())
            }
        })
    }


    fun ImageView.loadThumbnail(videoUrl: String , frame:Long = 2000) {
        Glide.with(context).setDefaultRequestOptions(RequestOptions().frame(frame)).load(videoUrl).into(this)
    }

    @SuppressLint("IntentReset")
    fun pickVideo(requestCode: Int, context: Activity) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "video/*"
        context.startActivityForResult(intent, requestCode)
    }

    fun pickDocument(requestCode: Int, context: Activity) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/*"
        context.startActivityForResult(intent, requestCode)
    }

    fun ImageView.loadImageFromResource(resourceId: Int) {
        Glide.with(this.context)
            .load(resourceId)
            .into(this)
    }

    fun formatDateFromMillis(milliseconds: Long): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val date = Date(milliseconds)
        return dateFormat.format(date)
    }

    fun getSortedKeys(userKey: String, currentUserKey: String): String {
        val keys = listOf(userKey, currentUserKey).sorted()
        return "${keys[0]}${keys[1]}"
    }
















}