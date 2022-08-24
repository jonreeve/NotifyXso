package com.wasabicode.notifyxso.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.wasabicode.notifyxso.app.config.Configuration
import com.wasabicode.notifyxso.app.config.ConfigurationVO
import com.wasabicode.notifyxso.app.config.PreferredIcon
import java.text.DecimalFormat

@Composable
fun ConfigurationUi(config: Configuration?) {
    if (config == null) {
        MdcTheme {
            CircularProgressIndicator(Modifier.padding(64.dp))
        }
    }
    else {
        Column(
            Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Forward\nNotifications",
                textAlign = TextAlign.Center,
                modifier = Modifier.align(CenterHorizontally)
            )
            Switch(
                checked = config.enabled,
                onCheckedChange = {},
                modifier = Modifier.align(CenterHorizontally)
            )
            SectionHeader("Server")
            Server(config)
            SectionHeader("Appearance")
            Appearance(config)
            SectionHeader("Filter")

        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(text = text, style = MaterialTheme.typography.subtitle2, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun Server(config: Configuration) {
    Row {
        TextField(
            value = config.host,
            label = { Text("Host") },
            onValueChange = {},
            modifier = Modifier
                .weight(3f)
                .padding(end = 4.dp)
        )
        TextField(
            value = config.port.toString(),
            label = { Text("Port") },
            onValueChange = {},
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun Appearance(config: Configuration) {
    val decimalFormat = DecimalFormat.getNumberInstance()
    Row {
        TextField(
            value = decimalFormat.format(config.durationSecs),
            label = { Text("Duration (secs)") },
            onValueChange = {},
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
        )
        Text("Icon:")
        IconDropDown(config, onSelected = {})
    }
}

@Composable
private fun IconDropDown(config: Configuration, onSelected: (PreferredIcon) -> Unit) {
    val icons = PreferredIcon.values()
    val selected = config.preferredIcon.name
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier.wrapContentWidth(CenterHorizontally)
    ) {
        Row(modifier = Modifier
            .clickable(onClick = { expanded = true })
            .background(MaterialTheme.colors.surface)
        ) {
            Text(selected)
            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Change Icon")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colors.surface)
        ) {
            icons.forEach { icon ->
                DropdownMenuItem(onClick = {
                    onSelected(icon)
                    expanded = false
                }) {
                    Text(text = icon.name)
                }
            }
        }
    }
}

@Preview(name = "Loaded", widthDp = 320, heightDp = 400)
@Composable
private fun PreviewLoaded() {
    ConfigurationUi(config = ConfigurationVO())
}

@Preview(name = "Loading", widthDp = 320, heightDp = 400)
@Composable
private fun PreviewLoading() {
    ConfigurationUi(config = null)
}
