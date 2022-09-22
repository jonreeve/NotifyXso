package com.wasabicode.notifyxso.app.features.filter

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wasabicode.notifyxso.app.R
import com.wasabicode.notifyxso.app.features.filter.FilterViewModel.Intention
import com.wasabicode.notifyxso.app.features.filter.FilterViewModel.Intention.UpdateExclusions
import com.wasabicode.notifyxso.app.features.filter.FilterViewModel.UiState
import com.wasabicode.notifyxso.app.shared.ui.LoadingUi
import kotlinx.coroutines.flow.Flow

@Composable
fun FilterScreen(viewModel: FilterViewModel = viewModel()) = FilterScreen(uiStateFlow = viewModel.uiState, act = viewModel::act)

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun FilterScreen(
    uiStateFlow: Flow<UiState>,
    act: (Intention) -> Unit = {},
) {
    val uiState = uiStateFlow.collectAsStateWithLifecycle(initialValue = UiState.Loading)
    when (val currentState = uiState.value) {
        is UiState.Loading -> LoadingUi()
        is UiState.Content -> ContentUi(currentState, act)
    }
}

@Composable
fun ContentUi(
    currentState: UiState.Content,
    act: (Intention) -> Unit = {},
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        TextField(
            value = currentState.exclusions,
            label = { Text(stringResource(R.string.config_label_exclusions)) },
            onValueChange = { act(UpdateExclusions(it)) },
            modifier = Modifier.fillMaxWidth()
        )
        CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
            Text(
                text = stringResource(R.string.config_label_exclusions_hint),
                style = MaterialTheme.typography.caption,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
