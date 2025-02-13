package com.barcode.scan

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.media.Image
import android.view.View
import com.google.mlkit.vision.barcode.common.Barcode

class BarcodeOverlay(context: Context) : View(context) {
    private var barcodes = listOf<Barcode>()
    private var scale = 1.0f
    private var traslX = 0.0f
    private var traslY = 0.0f
    private val mRectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#80418BFA")
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            for (barcode in barcodes) {
                barcode.boundingBox?.let { boundingBox ->
                    val rect = translateRect(boundingBox)
                    canvas.drawRect(rect, mRectPaint)

                }
            }
        }
    }

    fun update(scanResult: Image, codes: List<Barcode>) {
        val pw: Float
        val ph: Float
        if (isPortraitMode()) {
            pw = scanResult.height.toFloat()
            ph = scanResult.width.toFloat()
        } else {
            pw = scanResult.width.toFloat()
            ph = scanResult.height.toFloat()
        }
        val vw = width.toFloat()
        val vh = height.toFloat()
        val pictureAspectRatio = pw / ph
        val viewAspectRatio = vw / vh
        if (pictureAspectRatio > viewAspectRatio) {
            scale = vh / ph
            traslX = (pw * scale - vw) / 2
            traslY = 0f
        } else {
            scale = vw / pw
            traslX = 0f
            traslY = (ph * scale - vh) / 2
        }
        barcodes = codes
        invalidate()
    }

    private fun isPortraitMode(): Boolean {
        val orientation: Int = resources.configuration.orientation
        return orientation == Configuration.ORIENTATION_PORTRAIT
    }

    private fun translateRect(rect: Rect) = RectF(
        rect.left * scale,
        rect.top * scale,
        rect.right * scale,
        rect.bottom * scale
    ).apply {
        offset(-traslX, -traslY)
    }
}