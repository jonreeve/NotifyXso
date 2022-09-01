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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.material.composethemeadapter.MdcTheme
import com.wasabicode.notifyxso.app.MainViewModel.ViewState
import com.wasabicode.notifyxso.app.config.PreferredIcon
import com.wasabicode.notifyxso.app.config.Server
import java.text.DecimalFormat

@Composable
fun ConfigurationUi(
    viewState: ViewState,
    onForwardingChanged: (Boolean) -> Unit = {},
    onServerChanged: (host: String, port: String) -> Unit = { _, _ -> },
    onDurationChanged: (durationSecs: Float) -> Unit = {},
    onIconChanged: (icon: PreferredIcon) -> Unit = {},
    onExclusionsChanged: (exclusions: String) -> Unit = {},
    onTestNotificationButtonClicked: () -> Unit = {},
    onPermissionButtonClicked: () -> Unit = {}
) {
    when (viewState) {
        is ViewState.Loading -> LoadingUi()
        is ViewState.NoPermission -> LoadingUi() // TODO
        is ViewState.Content -> ContentUi(
            viewState,
            onForwardingChanged,
            onServerChanged,
            onDurationChanged,
            onIconChanged,
            onExclusionsChanged,
            onTestNotificationButtonClicked,
            onPermissionButtonClicked,
        )
    }
}

@Composable
private fun LoadingUi() {
    MdcTheme {
        CircularProgressIndicator(Modifier.padding(64.dp))
    }
}

@Composable
private fun ContentUi(
    viewState: ViewState.Content,
    onForwardingChanged: (Boolean) -> Unit = {},
    onServerChanged: (host: String, port: String) -> Unit = { _, _ -> },
    onDurationChanged: (durationSecs: Float) -> Unit = {},
    onIconChanged: (icon: PreferredIcon) -> Unit = {},
    onExclusionsChanged: (exclusions: String) -> Unit = {},
    onTestNotificationButtonClicked: () -> Unit = {},
    onPermissionButtonClicked: () -> Unit = {}
) {
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
                ForwardingSwitch(viewState.enabled, onForwardingChanged)
                SectionHeader("Server")
                ServerConfig(viewState.host, viewState.port, onServerChanged)
                SectionHeader("Appearance")
                AppearanceConfig(viewState.duration, viewState.icon, onDurationChanged, onIconChanged)
                SectionHeader("Filter")
                FilterConfig(viewState.exclusions, onExclusionsChanged)
                Buttons(onTestNotificationButtonClicked, onPermissionButtonClicked)
            }
        }
    }
}

@Composable
private fun ForwardingSwitch(
    enabled: Boolean,
    onForwardingChanged: (Boolean) -> Unit
) {
    Text(
        text = "Forward\nNotifications",
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Switch(
        checked = enabled,
        onCheckedChange = onForwardingChanged,
        modifier = Modifier.fillMaxWidth()
    )
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
private fun ServerConfig(
    host: String,
    port: String,
    onChange: (host: String, port: String) -> Unit
) {
    Row {
        TextField(
            value = host,
            label = { Text("Host") },
            onValueChange = { onChange(it, port) },
            modifier = Modifier
                .weight(3f)
                .padding(end = 4.dp)
        )
        TextField(
            value = port,
            label = { Text("Port") },
            onValueChange = { onChange(host, it) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AppearanceConfig(
    duration: String,
    icon: PreferredIcon,
    onDurationChanged: (durationSecs: Float) -> Unit,
    onIconChanged: (icon: PreferredIcon) -> Unit
) {
    val decimalFormat = DecimalFormat.getNumberInstance()
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = duration,
            label = { Text("Duration (secs)") },
            onValueChange = { text ->
                val newDuration = runCatching { decimalFormat.parse(text) }.getOrNull()?.toFloat()
                onDurationChanged(newDuration ?: 0f)
            },
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
                Text(
                    "Icon:",
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            IconDropDown(icon, onSelected = onIconChanged)
        }
    }
}

@Composable
private fun IconDropDown(icon: PreferredIcon, onSelected: (PreferredIcon) -> Unit) {
    val icons = PreferredIcon.values()
    val selected = icon.name
    var expanded by remember { mutableStateOf(false) }

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

@Composable
private fun FilterConfig(
    exclusions: String,
    onExclusionsChanged: (exclusions: String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = exclusions,
        label = { Text("Exclusions") },
        onValueChange = { onExclusionsChanged(it) },
        modifier = Modifier.fillMaxWidth()
    )
    CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
        Text(
            text = "One per line, notifications containing this will be ignored",
            style = MaterialTheme.typography.caption,
            textAlign = TextAlign.Center,
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun Buttons(onTestNotificationButtonClicked: () -> Unit, onPermissionButtonClicked: () -> Unit) {
    Button(
        onClick = onTestNotificationButtonClicked,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Test Notification".toUpperCase(Locale.current))
    }
    Button(
        onClick = onPermissionButtonClicked,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Allow Reading Notifications".toUpperCase(Locale.current))
    }
}

@Preview(name = "Loaded", widthDp = 320, heightDp = 700)
@Composable
private fun PreviewLoaded() {
    ConfigurationUi(
        viewState = ViewState.Content(
            enabled = false,
            host = "192.,168.16.8",
            port = "43210",
            duration = "2",
            icon = PreferredIcon.Default,
            exclusions = ""
        )
    )
}

@Preview(name = "Loading", widthDp = 320, heightDp = 160)
@Composable
private fun PreviewLoading() {
    ConfigurationUi(viewState = ViewState.Loading)
}
