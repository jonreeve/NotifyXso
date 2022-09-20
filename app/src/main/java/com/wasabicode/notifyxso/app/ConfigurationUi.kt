package com.wasabicode.notifyxso.app

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.material.composethemeadapter.MdcTheme
import com.wasabicode.notifyxso.app.MainViewModel.Intention
import com.wasabicode.notifyxso.app.MainViewModel.Intention.*
import com.wasabicode.notifyxso.app.MainViewModel.UiState
import com.wasabicode.notifyxso.app.config.PreferredIcon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun ConfigurationUi(
    uiStateFlow: Flow<UiState>,
    act: (Intention) -> Unit = {}
) {
    val uiState = uiStateFlow.collectAsStateWithLifecycle(initialValue = UiState.Loading)
    when (val currentState = uiState.value) {
        is UiState.Loading -> LoadingUi()
        is UiState.NoPermission -> NoPermissionUi()
        is UiState.Content -> ContentUi(currentState, act)
    }
}

@Preview(name = "Loading", widthDp = 320, heightDp = 160)
@Composable
private fun LoadingUi() {
    MdcTheme {
        Box(modifier = Modifier.wrapContentSize()) {
            CircularProgressIndicator(Modifier.padding(64.dp))
        }
    }
}

@Preview(name = "No Permission", widthDp = 320, heightDp = 160)
@Composable
fun NoPermissionUi() {
    MdcTheme {
        Box(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp)
        ) {
            PermissionButton()
        }
    }
}

@Composable
private fun ContentUi(
    uiState: UiState.Content,
    act: (Intention) -> Unit = {}
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
                ForwardingSwitch(uiState.enabled) { act(UpdateForwardingEnabled(it)) }
                SectionHeader("Server")
                ServerConfig(
                    host = uiState.host,
                    port = uiState.port,
                    onHostChanged = { act(UpdateHost(it)) },
                    onPortChanged = { act(UpdatePort(it)) }
                )
                SectionHeader("Appearance")
                AppearanceConfig(
                    duration = uiState.duration,
                    icon = uiState.icon,
                    onDurationChanged = { act(UpdateDuration(it)) },
                    onIconChanged = { act(UpdateIcon(it)) }
                )
                SectionHeader("Filter")
                FilterConfig(uiState.exclusions) { act(UpdateExclusions(it)) }
                TestNotificationButton()
                PermissionButton()
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
    onHostChanged: (host: String) -> Unit = {},
    onPortChanged: (port: String) -> Unit = {},
) {
    Row {
        TextField(
            value = host,
            label = { Text("Host") },
            onValueChange = { onHostChanged(it) },
            modifier = Modifier
                .weight(3f)
                .padding(end = 4.dp)
        )
        TextField(
            value = port,
            label = { Text("Port") },
            onValueChange = { onPortChanged(it) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun AppearanceConfig(
    duration: String,
    icon: PreferredIcon,
    onDurationChanged: (duration: String) -> Unit,
    onIconChanged: (icon: PreferredIcon) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        TextField(
            value = duration,
            label = { Text("Duration (secs)") },
            onValueChange = onDurationChanged,
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
    onExclusionsChanged: (exclusions: String) -> Unit
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun TestNotificationButton() {
    val context = LocalContext.current
    Button(
        onClick = { TestNotification().show(context) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Test Notification".toUpperCase(Locale.current))
    }
}

@Composable
private fun PermissionButton() {
    val context = LocalContext.current
    Button(
        onClick = { context.startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")) },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Allow Reading Notifications".toUpperCase(Locale.current))
    }
}

@Preview(name = "Content", widthDp = 320, heightDp = 700)
@Composable
private fun PreviewContent() {
    ContentUi(
        uiState = UiState.Content(
            enabled = false,
            host = "192.,168.16.8",
            port = "43210",
            duration = "2",
            icon = PreferredIcon.Default,
            exclusions = ""
        )
    )
}