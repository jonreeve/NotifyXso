package com.wasabicode.notifyxso.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.wasabicode.notifyxso.app.MainViewModel.Intention.*
import com.wasabicode.notifyxso.app.config.Configuration
import com.wasabicode.notifyxso.app.config.PreferredIcon
import com.wasabicode.notifyxso.app.config.Server
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var viewModel: MainViewModel

    private lateinit var config: Configuration

    private lateinit var composeView: ComposeView

    private val enableOnStartArg by lazy { intent.getBooleanExtra(EXTRA_ENABLE_ON_START, false) }
    private val hostArg by lazy { intent.getStringExtra(EXTRA_HOST) }
    private val portArg by lazy { intent.getIntExtra(EXTRA_PORT, -1).takeIf { it >= 0 } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = MainViewModel(application as App)

        config = (application as App).configuration
        composeView = findViewById(R.id.compose)

        processArgs()
        observeViewModel()
    }

    private fun processArgs() {
        if (hostArg != null || portArg != null) {
            viewModel.input(UpdateServer(Server(host = hostArg ?: config.server.host, port = portArg ?: config.server.port)))
        }
        if (enableOnStartArg) {
            viewModel.input(UpdateForwardingEnabled(true))
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest {
                    composeView.setContent {
                        ConfigurationUi(
                            config = it,
                            onForwardingChanged = ::onForwardingChanged,
                            onServerChanged = ::onServerChanged,
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

    private fun onServerChanged(server: Server) {
        viewModel.input(UpdateServer(server))
    }

    private fun onDurationChanged(durationSecs: Float) {
        viewModel.input(UpdateDuration(durationSecs))
    }

    private fun onIconChanged(icon: PreferredIcon) {
        viewModel.input(UpdateIcon(icon))
    }

    private fun onExclusionsChanged(exclusions: Set<String>) {
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
