package com.atta.chatspherapp.ui.activities.room

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.net.Uri
import android.util.AttributeSet
import android.view.MotionEvent
import com.github.chrisbanes.photoview.PhotoView
import java.io.File
import java.io.FileOutputStream

class DrawingOnImageView(context: Context, attrs: AttributeSet?) : PhotoView(context, attrs) {

    lateinit var drawCanvas: Canvas
    private lateinit var canvasBitmap: Bitmap
    private val drawPaint: Paint = Paint()
    private var lastX: Float = 0f
    private var lastY: Float = 0f
    private var scaleFactorX = 1f
    private var scaleFactorY = 1f
    private var selectedColor = Color.YELLOW

    init {
        setupDrawing()
    }

    private fun setupDrawing() {
        drawPaint.isAntiAlias = true
        drawPaint.strokeWidth = 5f
        drawPaint.style = Paint.Style.STROKE
        drawPaint.strokeJoin = Paint.Join.ROUND
        drawPaint.strokeCap = Paint.Cap.ROUND
        drawPaint.color = selectedColor
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (::canvasBitmap.isInitialized) {
            canvasBitmap.recycle()
        }
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(canvasBitmap, 0f, 0f, null) // Draw the combined bitmap
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x / scaleFactorX
        val touchY = event.y / scaleFactorY

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                drawCanvas.drawPoint(touchX, touchY, drawPaint)
                lastX = touchX
                lastY = touchY
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                drawCanvas.drawLine(lastX, lastY, touchX, touchY, drawPaint)
                lastX = touchX
                lastY = touchY
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                drawCanvas.drawLine(lastX, lastY, touchX, touchY, drawPaint)
                invalidate()
            }
        }
        return true
    }

    fun saveImageToFile(context: Context, originalBitmap: Bitmap): Uri? {
        val publicDir=context.filesDir
        val fileName = "${System.currentTimeMillis()}.png"
        val file = File(publicDir, fileName)
        return try {
            FileOutputStream(file).use { fos ->
                originalBitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            }
            Uri.fromFile(file) // Return the URI of the saved image
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun setColor(color: Int) {
        selectedColor = color
        drawPaint.color = selectedColor
        invalidate()
    }

    fun clearCanvas() {
        drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        invalidate()
    }
}