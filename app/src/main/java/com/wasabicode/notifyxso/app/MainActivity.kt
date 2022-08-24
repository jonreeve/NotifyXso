package com.wasabicode.notifyxso.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.compose.ui.platform.ComposeView
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.wasabicode.notifyxso.app.config.Configuration
import com.wasabicode.notifyxso.app.config.PreferredIcon
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.DecimalFormat

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var viewModel: MainViewModel

    private lateinit var config: Configuration
    private val decimalFormat = DecimalFormat.getNumberInstance()

    private lateinit var switchEnable: SwitchCompat
    private lateinit var editTextHost: EditText
    private lateinit var editTextPort: EditText
    private lateinit var editTextDuration: EditText
    private lateinit var iconSpinner: Spinner
    private lateinit var editTextExclusions: EditText
    private lateinit var testNotificationButton: Button
    private lateinit var permissionButton: Button
    private lateinit var composeView: ComposeView

    private val enableOnStartArg by lazy { intent.getBooleanExtra(EXTRA_ENABLE_ON_START, false) }
    private val hostArg by lazy { intent.getStringExtra(EXTRA_HOST) }
    private val portArg by lazy { intent.getIntExtra(EXTRA_PORT, -1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = MainViewModel(application as App)

        config = (application as App).configuration

        processArgs()
        findViews()
        observeViewModel()
        initViewValues()
        initListeners()
    }

    private fun processArgs() {
        if (enableOnStartArg) {
            config.enabled = true
        }
        hostArg?.let { config.host = it }
        portArg.takeIf { it >= 0 }?.let { config.port = it }
    }

    private fun findViews() {
        switchEnable = findViewById(R.id.switch_enable)
        editTextHost = findViewById(R.id.edit_text_host)
        editTextPort = findViewById(R.id.edit_text_port)
        editTextDuration = findViewById(R.id.edit_text_duration)
        iconSpinner = findViewById(R.id.notification_icon_spinner)
        editTextExclusions = findViewById(R.id.edit_text_exclusions)
        testNotificationButton = findViewById(R.id.create_notification_button)
        permissionButton = findViewById(R.id.permission_button)
        composeView = findViewById(R.id.compose)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collectLatest {
                    composeView.setContent { ConfigurationUi(it) }
                }
            }
        }
    }

    private fun initViewValues() {
        switchEnable.isChecked = config.enabled
        editTextHost.setText(config.host)
        editTextPort.setText(config.port.toString())
        editTextDuration.setText(decimalFormat.format(config.durationSecs))
        iconSpinner.setSelection(config.preferredIcon.ordinal)
        editTextExclusions.setText(config.exclusions.joinToString(separator = "\n"))
    }

    private fun initListeners() {
        testNotificationButton.setOnClickListener { TestNotification().show(this) }
        permissionButton.setOnClickListener {
            startActivity(Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
        }
        switchEnable.setOnCheckedChangeListener { _, isChecked -> config.enabled = isChecked }
        editTextHost.doAfterTextChanged {
            config.host = it.toString()
        }
        editTextPort.doAfterTextChanged {
            runCatching { Integer.parseInt(it.toString()) }.getOrNull()?.let { config.port = it }
        }
        editTextDuration.doAfterTextChanged {
            runCatching { decimalFormat.parse(it.toString()) }.getOrNull()?.toFloat()?.let {
                config.durationSecs = it
            }
        }
        iconSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                config.preferredIcon = PreferredIcon.values()[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                config.preferredIcon = PreferredIcon.Default
            }
        }
        editTextExclusions.doAfterTextChanged {
            config.exclusions = it?.split("\n")?.toSet() ?: emptySet()
        }
    }

    companion object {
        const val EXTRA_ENABLE_ON_START = "on"
        const val EXTRA_HOST = "host"
        const val EXTRA_PORT = "port"
    }
}
