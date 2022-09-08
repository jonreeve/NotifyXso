package com.wasabicode.notifyxso.app

import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wasabicode.notifyxso.app.MainViewModel.Intention.*
import com.wasabicode.notifyxso.app.config.Configuration
import com.wasabicode.notifyxso.app.config.PreferredIcon
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import kotlin.reflect.KClass

class MainViewModel(private val app: App, private val configurationRepo: ConfigurationRepo, private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel() {
    private val decimalFormat = DecimalFormat.getNumberInstance()

    private val editState = MutableStateFlow(UiState.Content(Configuration(), decimalFormat))
    val uiState: StateFlow<UiState> = flow {
        val canSeeNotifications = NotificationManagerCompat.getEnabledListenerPackages(app).contains(app.packageName)
        if (canSeeNotifications) {
            // We don't need to listen to upstream changes; we're the only editor, and we want to keep our local edits
            val initialConfig = configurationRepo.configuration.first()
            editState.value = UiState.Content(initialConfig, decimalFormat)
            emitAll(editState)
        } else {
            emit(UiState.NoPermission)
        }
    }.stateIn(viewModelScope, WhileSubscribed(), UiState.Loading)

    fun input(intention: Intention) = viewModelScope.launch(ioDispatcher) {
        when (intention) {
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
            is UpdateExclusions -> {
                editState.update { it.copy(exclusions = intention.exclusions) }
                updateConfig { copy(exclusions = intention.exclusions.lines().toSet()) }
            }
        }
    }

    private fun updateConfig(update: Configuration.() -> Configuration) {
        configurationRepo.update(update)
    }

    inline fun <T : Any, reified U : T> MutableStateFlow<T>.update(type: KClass<U>, block: U.() -> T?) {
        if (type.isInstance(value)) {
            (value as U).run(block)?.let { value = it }
        }
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
            val exclusions: String
        ) : UiState {
            constructor(config: Configuration, decimalFormat: NumberFormat) : this(
                enabled = config.enabled,
                host = config.host,
                port = config.port.toString(),
                duration = decimalFormat.format(config.durationSecs),
                exclusions = config.exclusions.joinToString(separator = "\n"),
                icon = config.preferredIcon
            )
        }
    }

    sealed interface Intention {
        data class UpdateForwardingEnabled(val enabled: Boolean) : Intention
        data class UpdateHost(val host: String) : Intention
        data class UpdatePort(val port: String) : Intention
        data class UpdateDuration(val duration: String) : Intention
        data class UpdateIcon(val icon: PreferredIcon) : Intention
        data class UpdateExclusions(val exclusions: String) : Intention
    }
}
