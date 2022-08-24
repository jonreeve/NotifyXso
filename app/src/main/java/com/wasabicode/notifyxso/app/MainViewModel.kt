package com.wasabicode.notifyxso.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wasabicode.notifyxso.app.config.Configuration
import com.wasabicode.notifyxso.app.config.ConfigurationVO
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel(app: App, ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel() {
    private val _state = MutableStateFlow<Configuration?>(null)
    val state: StateFlow<Configuration?> = _state

    init {
        viewModelScope.launch(ioDispatcher) {
            _state.value = ConfigurationVO(app.configuration)
        }
    }
}
