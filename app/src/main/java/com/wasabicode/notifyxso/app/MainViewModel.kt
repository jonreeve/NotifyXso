package com.wasabicode.notifyxso.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wasabicode.notifyxso.app.MainViewModel.Intention.*
import com.wasabicode.notifyxso.app.config.Configuration
import com.wasabicode.notifyxso.app.config.PreferredIcon
import com.wasabicode.notifyxso.app.config.Server
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat

class MainViewModel(private val app: App, private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel() {
    private val _viewState = MutableStateFlow<ViewState>(ViewState.Loading)
    val viewState: StateFlow<ViewState> = _viewState

    private val decimalFormat = DecimalFormat.getNumberInstance()

    init {
        viewModelScope.launch(ioDispatcher) {
            // TODO see if we can check if we have permission and are listening for notifications with this:
            // NotificationManagerCompat.getEnabledListenerPackages()
            _viewState.value = ViewState.Content(app.configuration, decimalFormat)
        }
    }

    fun input(intention: Intention) = viewModelScope.launch(ioDispatcher) {
        when (intention) {
            is UpdateForwardingEnabled -> updateConfig { enabled = intention.enabled }
            is UpdateServer -> updateConfig { server = Server(intention.host, intention.port.toIntOrNull() ?: 0) }
            is UpdateDuration -> updateConfig { durationSecs = intention.durationSecs }
            is UpdateIcon -> updateConfig { preferredIcon = intention.preferredIcon }
            is UpdateExclusions -> updateConfig { exclusions = intention.exclusions.lines().toSet() }
        }
    }

    private fun updateConfig(update: Configuration.() -> Unit) {
        app.configuration.update()
        _viewState.value = ViewState.Content(app.configuration, decimalFormat)
    }

    sealed interface ViewState {
        object NoPermission : ViewState
        object Loading : ViewState
        data class Content(
            val enabled: Boolean,
            val host: String,
            val port: String,
            val duration: String,
            val icon: PreferredIcon,
            val exclusions: String
        ) : ViewState {
            constructor(config: Configuration, decimalFormat: NumberFormat) : this(
                enabled = config.enabled,
                host = config.server.host,
                port = config.server.port.toString(),
                duration = decimalFormat.format(config.durationSecs),
                exclusions = config.exclusions.joinToString(separator = "\n"),
                icon = config.preferredIcon
            )
        }
    }

    sealed interface Intention {
        data class UpdateForwardingEnabled(val enabled: Boolean) : Intention
        data class UpdateServer(val host: String, val port: String) : Intention
        data class UpdateDuration(val durationSecs: Float) : Intention
        data class UpdateIcon(val preferredIcon: PreferredIcon) : Intention
        data class UpdateExclusions(val exclusions: String) : Intention
    }
}
