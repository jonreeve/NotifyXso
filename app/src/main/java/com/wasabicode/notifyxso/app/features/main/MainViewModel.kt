package com.wasabicode.notifyxso.app.features.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wasabicode.notifyxso.app.config.Configuration
import com.wasabicode.notifyxso.app.config.PreferredIcon
import com.wasabicode.notifyxso.app.features.main.MainViewModel.Intention.*
import com.wasabicode.notifyxso.app.shared.ConfigurationRepo
import com.wasabicode.notifyxso.app.shared.coroutines.AppDispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import javax.inject.Inject

@HiltViewModel
class MainViewModel constructor(
    private val canSeeNotifications: CanSeeNotificationsUseCase,
    private val configurationRepo: ConfigurationRepo,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    @Inject constructor (
        canSeeNotifications: CanSeeNotificationsUseCase,
        configurationRepo: ConfigurationRepo,
        appDispatchers: AppDispatchers
    ) : this(
        canSeeNotifications = canSeeNotifications,
        configurationRepo = configurationRepo,
        ioDispatcher = appDispatchers.IO
    )

    private val decimalFormat = DecimalFormat.getNumberInstance()

    private val editState = MutableStateFlow(UiState.Content(Configuration(), decimalFormat))
    val uiState: StateFlow<UiState> = flow {
        if (canSeeNotifications()) {
            // We don't need to listen to upstream changes while we're open, just our edits. Nothing else can change it while we're here
            val initialConfig = configurationRepo.configuration.first()
            editState.value = UiState.Content(initialConfig, decimalFormat)
            emitAll(editState)
        } else {
            emit(UiState.NoPermission)
        }
    }.stateIn(viewModelScope, WhileSubscribed(), UiState.Loading)

    fun act(intention: Intention) = viewModelScope.launch(ioDispatcher) {
        when (intention) {
            is HandleStartArguments -> {
                editState.update {
                    it.copy(
                        enabled = intention.enable,
                        host = intention.host ?: it.host,
                        port = intention.port?.toString() ?: it.port
                    )
                }
                updateConfig {
                    copy(
                        enabled = intention.enable,
                        host = intention.host ?: host,
                        port = intention.port ?: port
                    )
                }
            }
            is UpdateForwardingEnabled -> {
                editState.update { it.copy(enabled = intention.enabled) }
                updateConfig { copy(enabled = intention.enabled) }
            }
            is UpdateHost -> {
                editState.update { it.copy(host = intention.host) }
                updateConfig { copy(host = intention.host) }
            }
            is UpdatePort -> {
                editState.update { it.copy(port = intention.port) }
                updateConfig { copy(port = intention.port.toIntOrNull() ?: 0) }
            }
            is UpdateDuration -> {
                editState.update { it.copy(duration = intention.duration) }
                val newDuration = runCatching { decimalFormat.parse(intention.duration) }.getOrNull()?.toFloat()
                newDuration?.let { updateConfig { copy(durationSecs = it) } }
            }
            is UpdateIcon -> {
                editState.update { it.copy(icon = intention.icon) }
                updateConfig { copy(preferredIcon = intention.icon) }
            }
        }
    }

    private fun updateConfig(update: Configuration.() -> Configuration) {
        configurationRepo.update(update)
    }

    sealed interface UiState {
        object NoPermission : UiState
        object Loading : UiState
        data class Content(
            val enabled: Boolean,
            val host: String,
            val port: String,
            val duration: String,
            val icon: PreferredIcon,
            val exclusions: Int
        ) : UiState {
            constructor(config: Configuration, decimalFormat: NumberFormat) : this(
                enabled = config.enabled,
                host = config.host,
                port = config.port.toString(),
                duration = decimalFormat.format(config.durationSecs),
                exclusions = config.exclusions.size,
                icon = config.preferredIcon
            )
        }
    }

    sealed interface Intention {
        data class HandleStartArguments(val host: String?, val port: Int?, val enable: Boolean) : Intention
        data class UpdateForwardingEnabled(val enabled: Boolean) : Intention
        data class UpdateHost(val host: String) : Intention
        data class UpdatePort(val port: String) : Intention
        data class UpdateDuration(val duration: String) : Intention
        data class UpdateIcon(val icon: PreferredIcon) : Intention
    }
}
