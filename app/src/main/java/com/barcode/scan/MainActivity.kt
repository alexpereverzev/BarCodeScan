package com.barcode.scan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.barcode.scan.component.CameraPreview
import com.barcode.scan.component.ExternalScannerToggle
import com.barcode.scan.ui.theme.BarCodeScanTheme
import com.compose.ui.architecture.camerabarcode.component.ExternalScanner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

class MainActivity : ComponentActivity() {

    private val viewModel: ScanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var resultScan by remember { mutableStateOf("") }
            var usingExternalScan by remember { mutableStateOf(true) }
            var scanning by remember { mutableStateOf(true) }
            BarCodeScanTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding),
                        verticalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        if (!usingExternalScan) {
                            CameraPreviewScreen(
                                resultScan = { barCode ->
                                    if (scanning) {
                                        resultScan = barCode
                                        viewModel.action(ScanActions.Camera(number = barCode))
                                    }
                                }
                            )
                        } else {
                            ExternalScanner(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(350.dp),
                                result = { barCode ->
                                    if (scanning) {
                                        resultScan = barCode
                                        viewModel.action(ScanActions.External(number = barCode))
                                    }
                                }
                            )

                        }
                        Spacer(Modifier.height(100.dp))

                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = resultScan,
                            textAlign = TextAlign.Center
                        )

                        ExternalScannerToggle(text = "Using External Scanner",
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                            callBack = { isUsing ->
                                usingExternalScan = isUsing
                            })
                    }
                }
            }

            val uiState by viewModel.stateFlow.collectAsState()
            when (uiState) {
                ScanState.HandlingResult -> {
                    scanning = false
                }

                ScanState.Scanning -> {
                    scanning = true
                    resultScan = ""
                }
            }
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewScreen(
    modifier: Modifier = Modifier,
    resultScan: (barCode: String) -> Unit
) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    if (cameraPermissionState.status.isGranted) {
        Box(
            modifier
                .fillMaxWidth()
                .height(350.dp),
        ) {
            CameraPreview(
                Modifier.fillMaxSize(),
                barCodeListener = { barCode ->
                    resultScan.invoke(barCode)
                })
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .wrapContentSize()
                .widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
                // If the user has denied the permission but the rationale can be shown,
                // then gently explain why the app requires this permission
                "Looks like we need your camera to work." +
                        "Grant us permission."
            } else {
                // If it's the first time the user lands on this feature, or the user
                // doesn't want to be asked again for this permission, explain that the
                // permission is required
                "Looks like we need your camera to work. ✨\n" +
                        "Grant us permission. \uD83C\uDF89"
            }
            Text(textToShow, textAlign = TextAlign.Center)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                Text("Unleash the Camera!")
            }
        }
    }
}

