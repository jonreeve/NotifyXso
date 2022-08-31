package com.wasabicode.notifyxso.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.wasabicode.notifyxso.app.config.Configuration
import com.wasabicode.notifyxso.app.config.ConfigurationVO
import com.wasabicode.notifyxso.app.config.PreferredIcon
import com.wasabicode.notifyxso.app.config.Server
import java.text.DecimalFormat

@Composable
fun ConfigurationUi(
    config: Configuration?,
    onForwardingChanged: (Boolean) -> Unit = {},
    onServerChanged: (server: Server) -> Unit = {},
    onDurationChanged: (durationSecs: Float) -> Unit = {},
    onIconChanged: (icon: PreferredIcon) -> Unit = {},
    onExclusionsChanged: (exclusions: Set<String>) -> Unit = {},
    onTestNotificationButtonClicked: () -> Unit = {},
    onPermissionButtonClicked: () -> Unit = {}
) {
    if (config == null) {
        MdcTheme {
            CircularProgressIndicator(Modifier.padding(64.dp))
        }
    } else {
        MdcTheme {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            )
            {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Forward\nNotifications",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.align(CenterHorizontally)
                    )
                    Switch(
                        checked = config.enabled,
                        onCheckedChange = onForwardingChanged,
                        modifier = Modifier.align(CenterHorizontally)
                    )
                    SectionHeader("Server")
                    ServerConfig(config, onServerChanged)
                    SectionHeader("Appearance")
                    AppearanceConfig(config, onDurationChanged, onIconChanged)
                    SectionHeader("Filter")
                    TextField(
                        value = config.exclusions.joinToString(separator = "\n"),
                        label = { Text("Exclusions") },
                        onValueChange = { onExclusionsChanged(it.lines().toSet()) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                        Text(
                            text = "One per line, notifications containing this will be ignored",
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier
                                .align(CenterHorizontally)
                                .padding(bottom = 16.dp)
                        )
                    }
                    Button(
                        onClick = onTestNotificationButtonClicked,
                        modifier = Modifier.align(CenterHorizontally)
                    ) {
                        Text("Test Notification".toUpperCase(Locale.current))
                    }
                    Button(
                        onClick = onPermissionButtonClicked,
                        modifier = Modifier.align(CenterHorizontally)
                    ) {
                        Text("Allow Reading Notifications".toUpperCase(Locale.current))
                    }
                    Text(text = "a\na\na\na\na\na\na\na\na\na\na")
                }
            }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.subtitle2,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun ServerConfig(config: Configuration, onChange: (server: Server) -> Unit) {
    Row {
        TextField(
            value = config.server.host,
            label = { Text("Host") },
            onValueChange = { onChange(config.server.copy(host = it)) },
            modifier = Modifier
                .weight(3f)
                .padding(end = 4.dp)
        )
        TextField(
            value = config.server.port.toString(),
            label = { Text("Port") },
            onValueChange = { onChange(config.server.copy(port = it.toIntOrNull() ?: 0)) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AppearanceConfig(
    config: Configuration,
    onDurationChanged: (durationSecs: Float) -> Unit,
    onIconChanged: (icon: PreferredIcon) -> Unit
) {
    val decimalFormat = DecimalFormat.getNumberInstance()
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = decimalFormat.format(config.durationSecs),
            label = { Text("Duration (secs)") },
            onValueChange = { text ->
                val newDuration = runCatching { decimalFormat.parse(text) }.getOrNull()?.toFloat()
                onDurationChanged(newDuration ?: 0f)
            },
            modifier = Modifier
                .weight(1f)
                .padding(end = 4.dp)
        )
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                "Icon:",
                style = MaterialTheme.typography.caption,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
        IconDropDown(config, onSelected = onIconChanged)
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
        Row(
            modifier = Modifier
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

@Preview(name = "Loaded", widthDp = 320, heightDp = 500)
@Composable
private fun PreviewLoaded() {
    ConfigurationUi(config = ConfigurationVO())
}

@Preview(name = "Loading", widthDp = 320, heightDp = 400)
@Composable
private fun PreviewLoading() {
    ConfigurationUi(config = null)
}
