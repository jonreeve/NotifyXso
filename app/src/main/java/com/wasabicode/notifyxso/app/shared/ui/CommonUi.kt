package com.wasabicode.notifyxso.app.shared.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun LoadingUi() {
    Box(modifier = Modifier.fillMaxSize().wrapContentSize()) {
        CircularProgressIndicator(Modifier.padding(64.dp))
    }
}

@Preview(name = "Loading", widthDp = 320, heightDp = 160)
@Composable
private fun PreviewLoading() {
    AppTheme {
        LoadingUi()
    }
}
