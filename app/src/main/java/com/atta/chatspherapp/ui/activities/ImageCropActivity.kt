package com.atta.chatspherapp.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.atta.chatspherapp.R
import com.atta.chatspherapp.utils.NewUtils.setStatusBarColor
import com.yalantis.ucrop.UCrop
import java.io.File
import java.util.UUID


class ImageCropActivity : AppCompatActivity() {
    var uri: Uri?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_crop)
        setStatusBarColor(R.color.tool_bar_color)

        val bundle=intent.getStringExtra("DATA")
        uri= Uri.parse(bundle)

        val options = UCrop.Options()
        val destinationUri = Uri.fromFile(File(cacheDir, generateRandomFileName()))
        UCrop.of(uri!!, destinationUri)
            .withOptions(options)
            .withAspectRatio(0F, 0F)
            .useSourceImageAspectRatio()
            .withMaxResultSize(3000, 2000)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            UCrop.REQUEST_CROP -> {
                if (resultCode == RESULT_OK) {
                    val resultUri = UCrop.getOutput(data!!)
                    val resultIntent = Intent()
                    resultIntent.putExtra("RESULT", resultUri.toString())
                    setResult(RESULT_OK, resultIntent)
                } else if (resultCode == UCrop.RESULT_ERROR) {
                    val cropError = UCrop.getError(data!!)
                    Log.e("CropeActivity", "Crop error: $cropError")
                }
                finish()
            }
        }
    }

    private fun generateRandomFileName(): String {
        val randomFileName = StringBuilder(UUID.randomUUID().toString()).append(".jpg").toString()
        return randomFileName
    }
}