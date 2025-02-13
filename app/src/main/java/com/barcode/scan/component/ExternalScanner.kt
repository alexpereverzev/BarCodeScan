package com.compose.ui.architecture.camerabarcode.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun ExternalScanner(
    modifier: Modifier = Modifier,
    result: (String) -> Unit = {}
) {
    val textValue = remember {
        mutableStateOf("")
    }

    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = modifier
            .background(color = Color.Blue)
    ) {
        OutlinedTextField(
            value = textValue.value,
            onValueChange = {
                textValue.value = it.trim()
                result.invoke(it)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.Center)
                .focusRequester(focusRequester),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Preview
@Composable
private fun PreviewExternalScanner() {
    ExternalScanner()
}