package com.wasabicode.notifyxso.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.wasabicode.notifyxso.app.MainViewModel.Intention.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val enableOnStartArg by lazy { intent.getBooleanExtra(EXTRA_ENABLE_ON_START, false) }
    private val hostArg by lazy { intent.getStringExtra(EXTRA_HOST) }
    private val portArg by lazy { intent.getIntExtra(EXTRA_PORT, -1).takeIf { it >= 0 } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        processArgs()
        setContent {
            MainScreen(viewModel = viewModel)
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
