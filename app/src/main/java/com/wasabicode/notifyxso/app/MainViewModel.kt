package com.wasabicode.notifyxso.app

import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wasabicode.notifyxso.app.MainViewModel.Intention.*
import com.wasabicode.notifyxso.app.config.Configuration
import com.wasabicode.notifyxso.app.config.PreferredIcon
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.NumberFormat
import kotlin.reflect.KClass

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(private val app: App, private val configurationRepo: ConfigurationRepo, private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel() {
    private val decimalFormat = DecimalFormat.getNumberInstance()

    val viewState: StateFlow<ViewState> = permissionFlow().flatMapLatest { granted ->
        if (!granted) flowOf(ViewState.NoPermission as ViewState)
        else configurationRepo.configuration.map { ViewState.Content(it, decimalFormat) }
    }.stateIn(viewModelScope, WhileSubscribed(), ViewState.Loading)


    private fun permissionFlow() = flow {
        val canSeeNotifications = NotificationManagerCompat.getEnabledListenerPackages(app).contains(app.packageName)
        emit(canSeeNotifications)
    }

    fun input(intention: Intention) = viewModelScope.launch(ioDispatcher) {
        when (intention) {
            is UpdateForwardingEnabled -> updateConfig { copy(enabled = intention.enabled) }
            is UpdateHost -> updateConfig { copy(host = intention.host) }
            is UpdatePort -> updateConfig { copy(port = intention.port.toIntOrNull() ?: 0) }
            is UpdateDuration -> {
                val newDuration = runCatching { decimalFormat.parse(intention.duration) }.getOrNull()?.toFloat()
                newDuration?.let { updateConfig { copy(durationSecs = it) } }
            }
            is UpdateIcon -> updateConfig { copy(preferredIcon = intention.icon) }
            is UpdateExclusions -> updateConfig { copy(exclusions = intention.exclusions.lines().toSet()) }
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
