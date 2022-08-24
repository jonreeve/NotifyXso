package com.wasabicode.notifyxso.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.wasabicode.notifyxso.app.config.Configuration

@Composable
fun ConfigurationUi(config: Configuration?) {
    if (config == null) {
        MdcTheme {
            CircularProgressIndicator(Modifier.padding(64.dp))
        }
    }
}

@Preview(name = "Loading", widthDp = 320, heightDp = 400)
@Composable
private fun PreviewLoading() {
    ConfigurationUi(config = null)
}
