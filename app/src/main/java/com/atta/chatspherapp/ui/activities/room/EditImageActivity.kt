package com.atta.chatspherapp.ui.activities.room

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import com.atta.chatspherapp.R
import com.atta.chatspherapp.databinding.ActivityEditImageBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class EditImageActivity : AppCompatActivity() {
    lateinit var binding: ActivityEditImageBinding
    var isColorLayoutVisible = false
    var CROPE_IMAGE_REQUEST_CODE=121
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityEditImageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val imageUri=intent.getStringExtra("DATA")!!

        binding.photoView.setImageURI(Uri.parse(imageUri))

        binding.cropImg.setOnClickListener {
            binding.photoView.clearCanvas()
            val intent= Intent(this@EditImageActivity, ImageCropActivity::class.java)
            intent.putExtra("DATA",imageUri)
            startActivityForResult(intent,CROPE_IMAGE_REQUEST_CODE)
        }

        binding.editImg.setOnClickListener {
            if (isColorLayoutVisible) {
                hideColorLayout()
                binding.photoView.setZoomable(true)
            } else {
                visibleColorLayout()
                val myAnim: Animation = AnimationUtils.loadAnimation(this, R.anim.bounce_anim)
                binding.colorLinearLayout.startAnimation(myAnim)
                binding.photoView.setZoomable(false)
            }
            isColorLayoutVisible = !isColorLayoutVisible
        }

        binding.resetTv.setOnClickListener {
            binding.photoView.clearCanvas()
        }

        binding.yellowCard.setOnClickListener {
            selectedCardRadius(binding.yellowCard)
            binding.photoView.setColor(Color.YELLOW)
            resetOtherCards(binding.greenCard,binding.redCard,binding.blueCard)
            binding.editImg.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC_ATOP)
        }

        binding.greenCard.setOnClickListener {
            selectedCardRadius(binding.greenCard)
            binding.photoView.setColor(Color.GREEN)
            resetOtherCards(binding.yellowCard,binding.redCard,binding.blueCard)
            binding.editImg.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_ATOP)
        }

        binding.redCard.setOnClickListener{
            selectedCardRadius(binding.redCard)
            binding.photoView.setColor(Color.RED)
            resetOtherCards(binding.yellowCard,binding.greenCard,binding.blueCard)
            binding.editImg.setColorFilter(Color.RED, PorterDuff.Mode.SRC_ATOP)
        }

        binding.blueCard.setOnClickListener {
            selectedCardRadius(binding.blueCard)
            binding.photoView.setColor(Color.BLUE)
            resetOtherCards(binding.yellowCard,binding.greenCard,binding.redCard)
            binding.editImg.setColorFilter(Color.BLUE, PorterDuff.Mode.SRC_ATOP)
        }

        binding.clearImg.setOnClickListener {
            finish()
        }

        binding.sendImage.setOnClickListener {
            lifecycleScope.launch {

                val location = IntArray(2)
                binding.imageLinear.getLocationOnScreen(location)
                val bitmap = Bitmap.createBitmap(binding.imageLinear.width, binding.imageLinear.height, Bitmap.Config.ARGB_8888)

                val canvas = Canvas(bitmap)
                binding.imageLinear.draw(canvas)

                val uri = withContext(Dispatchers.IO) { binding.photoView.saveImageToFile(this@EditImageActivity, bitmap) }

                val resultIntent = Intent(this@EditImageActivity, ChatActivity::class.java)
                resultIntent.putExtra("RESULT", uri.toString())
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }


    }

    fun hideColorLayout(){
        binding.colorLinearLayout.visibility = View.GONE
        binding.yellowCard.visibility = View.GONE
        binding.greenCard.visibility = View.GONE
        binding.redCard.visibility = View.GONE
        binding.blueCard.visibility = View.GONE
        binding.resetTv.visibility = View.GONE
    }

    fun visibleColorLayout(){
        binding.colorLinearLayout.visibility = View.VISIBLE
        binding.yellowCard.visibility = View.VISIBLE
        binding.greenCard.visibility = View.VISIBLE
        binding.redCard.visibility = View.VISIBLE
        binding.blueCard.visibility = View.VISIBLE
        binding.resetTv.visibility = View.VISIBLE
    }

    fun resetOtherCards(cardView1: CardView, cardView2: CardView, cardView3: CardView){
        cardView1.radius=40f
        cardView2.radius=40f
        cardView3.radius=40f
    }

    fun selectedCardRadius(cardView: CardView){
        cardView.radius=10f
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CROPE_IMAGE_REQUEST_CODE -> {
                val bundleUri = data?.getStringExtra("RESULT")
                if (bundleUri!=null){
                    binding.photoView.setImageURI(Uri.parse(bundleUri))
                }
            }
        }
    }

}