package com.wasabicode.notifyxso.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.wasabicode.notifyxso.app.MainViewModel.Intention.*
import com.wasabicode.notifyxso.app.config.Configuration
import com.wasabicode.notifyxso.app.config.PreferredIcon
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var config: Configuration

    private val enableOnStartArg by lazy { intent.getBooleanExtra(EXTRA_ENABLE_ON_START, false) }
    private val hostArg by lazy { intent.getStringExtra(EXTRA_HOST) }
    private val portArg by lazy { intent.getIntExtra(EXTRA_PORT, -1).takeIf { it >= 0 } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = MainViewModel(application as App)

        config = (application as App).configuration

        processArgs()
        observeViewModel()
    }

    private fun processArgs() {
        hostArg?.let { viewModel.input(UpdateHost(host = it)) }
        portArg?.let { viewModel.input(UpdatePort(port = it.toString())) }
        if (enableOnStartArg) {
            viewModel.input(UpdateForwardingEnabled(true))
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collectLatest { state ->
                    setContent {
                        ConfigurationUi(
                            viewState = state,
                            onForwardingChanged = ::onForwardingChanged,
                            onHostChanged = ::onHostChanged,
                            onPortChanged = ::onPortChanged,
                            onDurationChanged = ::onDurationChanged,
                            onIconChanged = ::onIconChanged,
                            onExclusionsChanged = ::onExclusionsChanged,
                            onTestNotificationButtonClicked = ::showTestNotification,
                            onPermissionButtonClicked = ::launchNotificationPermissionSettings
                        )
                    }
                }
            }
        }
    }

    private fun onForwardingChanged(enabled: Boolean) {
        viewModel.input(UpdateForwardingEnabled(enabled))
    }

    private fun onHostChanged(host: String) {
        viewModel.input(UpdateHost(host))
    }

    private fun onPortChanged(port: String) {
        viewModel.input(UpdatePort(port))
    }

    private fun onDurationChanged(duration: String) {
        viewModel.input(UpdateDuration(duration))
    }

    private fun onIconChanged(icon: PreferredIcon) {
        viewModel.input(UpdateIcon(icon))
    }

    private fun onExclusionsChanged(exclusions: String) {
        viewModel.input(UpdateExclusions(exclusions))
    }

    private fun launchNotificationPermissionSettings() {
        startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
    }

    private fun showTestNotification() {
        TestNotification().show(this)
    }

    companion object {
        const val EXTRA_ENABLE_ON_START = "on"
        const val EXTRA_HOST = "host"
        const val EXTRA_PORT = "port"
    }
}
