package com.barcode.scan.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ExternalScannerToggle(
    modifier: Modifier = Modifier,
    text: String,
    callBack: (Boolean) -> Unit = {}
) {
    var checked by remember { mutableStateOf(true) }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, Modifier.weight(1.0f))
        Switch(
            checked = checked,
            onCheckedChange = {
                checked = it
                callBack.invoke(it)
            }
        )
    }
}

@Preview
@Composable
private fun PreviewExternalScannerToggle() {
    ExternalScannerToggle(text = "Using External Scanner")
}