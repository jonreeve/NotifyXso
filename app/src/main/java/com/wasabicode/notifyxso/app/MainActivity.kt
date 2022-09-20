package com.wasabicode.notifyxso.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.wasabicode.notifyxso.app.MainViewModel.Intention.*

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel

    private val enableOnStartArg by lazy { intent.getBooleanExtra(EXTRA_ENABLE_ON_START, false) }
    private val hostArg by lazy { intent.getStringExtra(EXTRA_HOST) }
    private val portArg by lazy { intent.getIntExtra(EXTRA_PORT, -1).takeIf { it >= 0 } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = MainViewModel(application as App, SharedPrefsConfigurationRepo(application))
        processArgs()
        setContent {
            ConfigurationUi(uiStateFlow = viewModel.uiState, act = viewModel::act)
        }
    }

    private fun processArgs() {
        viewModel.act(HandleStartArguments(hostArg, portArg, enableOnStartArg))
    }

    companion object {
        const val EXTRA_ENABLE_ON_START = "on"
        const val EXTRA_HOST = "host"
        const val EXTRA_PORT = "port"
    }
}
