package com.barcode.scan

import android.annotation.SuppressLint
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.atomic.AtomicBoolean

class BarcodeAnalyzer(
    private val overlay: BarcodeOverlay,
    val listener: (barcode: String) -> Unit
) :
    ImageAnalysis.Analyzer {
    private var isBusy = AtomicBoolean(false)

    @SuppressLint("UnsafeExperimentalUsageError", "UnsafeOptInUsageError")
    override fun analyze(image: ImageProxy) {
        if (isBusy.compareAndSet(false, true)) {
            image.image?.let { imageItem ->
                val visionImage =
                    InputImage.fromMediaImage(imageItem, image.imageInfo.rotationDegrees)
                val options = BarcodeScannerOptions.Builder().enableAllPotentialBarcodes().build()
                BarcodeScanning.getClient(options).process(visionImage)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result?.let { barcodes ->
                                overlay.update(visionImage.mediaImage!!, barcodes)
                                for (barcode in barcodes) {
                                    listener(barcode.rawValue ?: "")
                                }
                            }
                        } else {
                            Log.e("scanner", "failed to scan image: ${task.exception?.message}")
                        }
                        image.close()
                        isBusy.set(false)
                    }
            }
        } else {
            image.close()
        }
    }
}