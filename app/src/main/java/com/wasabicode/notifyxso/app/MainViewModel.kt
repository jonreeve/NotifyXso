package com.wasabicode.notifyxso.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wasabicode.notifyxso.app.MainViewModel.Intention.UpdateServer
import com.wasabicode.notifyxso.app.config.Configuration
import com.wasabicode.notifyxso.app.config.ConfigurationVO
import com.wasabicode.notifyxso.app.config.Server
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(private val app: App, ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel() {
    private val _state = MutableStateFlow<Configuration?>(null)
    val state: StateFlow<Configuration?> = _state

    init {
        viewModelScope.launch(ioDispatcher) {
            _state.value = ConfigurationVO(app.configuration)
        }
    }

    fun input(intention: Intention) = when (intention) {
        is UpdateServer -> {
            app.configuration.server = intention.server
            _state.value = ConfigurationVO(app.configuration)
        }
    }

    sealed interface Intention {
        data class UpdateServer(val server: Server) : Intention
    }
}
