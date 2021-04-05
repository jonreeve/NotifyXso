package com.wasabicode.notificationstoxso.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.widget.doAfterTextChanged
import java.text.DecimalFormat

class ConfigurationActivity : AppCompatActivity(R.layout.activity_configuration) {

    private lateinit var config: Configuration
    private val decimalFormat = DecimalFormat.getNumberInstance()

    private lateinit var switchEnable: SwitchCompat
    private lateinit var editTextHost: EditText
    private lateinit var editTextPort: EditText
    private lateinit var editTextDuration: EditText
    private lateinit var testNotificationButton: Button

    private val enableOnStartArg by lazy { intent.getBooleanExtra(EXTRA_ENABLE_ON_START, false) }
    private val hostArg by lazy { intent.getStringExtra(EXTRA_HOST) }
    private val portArg by lazy { intent.getIntExtra(EXTRA_PORT, -1) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        config = (application as App).configuration

        if (enableOnStartArg) { config.enabled = true }
        hostArg?.let { config.host = it }
        portArg.takeIf { it >= 0 }?.let { config.port = it }

        findViews()

        initViewValues()

        initListeners()
    }

    private fun findViews() {
        switchEnable = findViewById(R.id.switch_enable)
        editTextHost = findViewById(R.id.edit_text_host)
        editTextPort = findViewById(R.id.edit_text_port)
        editTextDuration = findViewById(R.id.edit_text_duration)
        testNotificationButton = findViewById(R.id.create_notification_button)
    }

    private fun initViewValues() {
        switchEnable.isChecked = config.enabled
        editTextHost.setText(config.host)
        editTextPort.setText(config.port.toString())
        editTextDuration.setText(decimalFormat.format(config.durationSecs))
    }

    private fun initListeners() {
        testNotificationButton.setOnClickListener { createTestNotification() }
        switchEnable.setOnCheckedChangeListener { _, isChecked -> config.enabled = isChecked }
        editTextHost.doAfterTextChanged {
            config.host = it.toString()
        }
        editTextPort.doAfterTextChanged {
            runCatching { Integer.parseInt(it.toString()) }.getOrNull()?.let { config.port = it }
        }
        editTextDuration.doAfterTextChanged {
            runCatching { decimalFormat.parse(it.toString()) }.getOrNull()?.toFloat()?.let {
                Log.v("JONDEBUG", "parsed $it")
                config.durationSecs = it
            }
        }
    }

    private fun createTestNotification() {
        val notificationManager = NotificationManagerCompat.from(this)

        //create notification channel for android Oreo and above devices.
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(CHANNEL_ID , "Default", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("My notification")
            .setContentText("Much longer text that cannot fit one line...")
//            .setStyle(NotificationCompat.BigTextStyle()
//                .bigText("Much longer text that cannot fit one line..."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }

    companion object {
        const val EXTRA_ENABLE_ON_START = "go"
        const val EXTRA_HOST = "host"
        const val EXTRA_PORT = "port"
    }
}

private const val CHANNEL_ID = "com.wasabicode.notificationstoxso.app.default"
private const val NOTIFICATION_ID = 9678
