package com.barcode.scan.component

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.barcode.scan.BarcodeAnalyzer
import com.barcode.scan.BarcodeOverlay
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { future ->
        future.addListener(
            {
                continuation.resume(future.get())
            },
            executor
        )
    }
}

val Context.executor: Executor
    get() = ContextCompat.getMainExecutor(this)
private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
private var imageAnalyzer: ImageAnalysis? = null
private val available: Semaphore = Semaphore(1, true)

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    scaleType: PreviewView.ScaleType = PreviewView.ScaleType.FILL_CENTER,
    cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA,
    barCodeListener: (barCode: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture =
        remember(context) { ProcessCameraProvider.getInstance(context) }
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }
    val previewView = PreviewView(context).apply {
        this.scaleType = scaleType
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
    // this customview create a rectangle over qrcode image
    val overlay = BarcodeOverlay(context)
    AndroidView(
        modifier = modifier,
        factory = { _ ->
            cameraProviderFuture.addListener({

                // ImageAnalysis
                imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)

                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor,
                            BarcodeAnalyzer(overlay) { barcode ->
                                available.acquire()
                                barCodeListener(barcode)
                                available.release()
                            })
                    }
            }, executor)

            // Preview
            val previewUseCase = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

            coroutineScope.launch {
                val cameraProvider = context.getCameraProvider()
                try {
                    // Must unbind the use-cases before rebinding them.
                    cameraProvider.unbindAll()

                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, previewUseCase, imageAnalyzer
                    )

                } catch (ex: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", ex)
                }
            }
            previewView
        }
    )
}