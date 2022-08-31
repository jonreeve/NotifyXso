package com.wasabicode.notifyxso.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wasabicode.notifyxso.app.MainViewModel.Intention.*
import com.wasabicode.notifyxso.app.config.Configuration
import com.wasabicode.notifyxso.app.config.ConfigurationVO
import com.wasabicode.notifyxso.app.config.PreferredIcon
import com.wasabicode.notifyxso.app.config.Server
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val app: App, private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel() {
    private val _state = MutableStateFlow<Configuration?>(null)
    val state: StateFlow<Configuration?> = _state

    init {
        viewModelScope.launch(ioDispatcher) {
            _state.value = ConfigurationVO(app.configuration)
        }
    }

    fun input(intention: Intention) = viewModelScope.launch(ioDispatcher) {
        when (intention) {
            is UpdateForwarding -> updateConfig { enabled = intention.enabled }
            is UpdateServer -> updateConfig { server = intention.server }
            is UpdateDuration -> updateConfig { durationSecs = intention.durationSecs }
            is UpdateIcon -> updateConfig { preferredIcon = intention.preferredIcon }
            is UpdateExclusions -> updateConfig { exclusions = intention.exclusions }
        }
    }

    private fun updateConfig(update: Configuration.() -> Unit) {
        app.configuration.update()
        _state.value = ConfigurationVO(app.configuration)
    }

    sealed interface Intention {
        data class UpdateForwarding(val enabled: Boolean) : Intention
        data class UpdateServer(val server: Server) : Intention
        data class UpdateDuration(val durationSecs: Float) : Intention
        data class UpdateIcon(val preferredIcon: PreferredIcon) : Intention
        data class UpdateExclusions(val exclusions: Set<String>) : Intention
    }
}
