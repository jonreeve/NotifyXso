package com.wasabicode.notifyxso.app.features.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wasabicode.notifyxso.app.shared.ConfigurationRepo
import com.wasabicode.notifyxso.app.shared.coroutines.AppDispatchers
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.flow.SharingStarted.Companion.WhileSubscribed
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilterViewModel(
    private val configurationRepo: ConfigurationRepo,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    @Inject constructor (
        configurationRepo: ConfigurationRepo,
        appDispatchers: AppDispatchers
    ) : this(
        configurationRepo = configurationRepo,
        ioDispatcher = appDispatchers.IO
    )

    private val editState = MutableStateFlow(UiState.Content(exclusions = ""))
    val uiState: StateFlow<UiState> = flow {
        val initialConfig = configurationRepo.configuration.first()
        editState.value = UiState.Content(initialConfig.exclusions.joinToString(separator = "\n"))
        emitAll(editState)
    }.stateIn(viewModelScope, WhileSubscribed(), UiState.Loading)

    fun act(intention: Intention) = viewModelScope.launch(ioDispatcher) {
        when (intention) {
            is Intention.UpdateExclusions -> {
                editState.update { it.copy(exclusions = intention.exclusions) }
                configurationRepo.update { copy(exclusions = intention.exclusions.lines().toSet()) }
            }
        }
    }

    sealed interface UiState {
        object Loading : UiState
        data class Content(
            val exclusions: String
        ) : UiState
    }

    sealed interface Intention {
        data class UpdateExclusions(val exclusions: String) : Intention
    }
}
