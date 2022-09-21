package com.wasabicode.notifyxso.app

import android.content.Context
import com.wasabicode.notifyxso.app.config.Configuration
import com.wasabicode.notifyxso.app.config.Configuration.Companion.Defaults
import com.wasabicode.notifyxso.app.config.PreferredIcon
import com.wasabicode.notifyxso.app.config.SharedPrefsDelegate
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

interface ConfigurationRepo {
    val configuration: Flow<Configuration>

    fun update(update: Configuration.() -> Configuration)
}

class SharedPrefsConfigurationRepo @Inject constructor(@ApplicationContext context: Context) : ConfigurationRepo {
    private val sharedPrefs = context.getSharedPreferences("config", Context.MODE_PRIVATE)
    private var enabled by SharedPrefsDelegate.boolean(sharedPrefs)
    private var host by SharedPrefsDelegate.string(sharedPrefs, defaultValue = Defaults.host)
    private var port by SharedPrefsDelegate.int(sharedPrefs, defaultValue = Defaults.port)
    private var durationSecs by SharedPrefsDelegate.float(sharedPrefs, defaultValue = Defaults.durationSecs)
    private var exclusions by SharedPrefsDelegate.stringSet(sharedPrefs, defaultValue = Defaults.exclusions)
    private var preferredIcon: PreferredIcon by SharedPrefsDelegate.int(sharedPrefs, defaultValue = Defaults.preferredIcon.ordinal)
        .mapped(
            get = { PreferredIcon.values()[it] },
            set = { it.ordinal }
        )

    private val currentConfig: Configuration
        get() = Configuration(
            enabled = enabled,
            host = host,
            port = port,
            durationSecs = durationSecs,
            exclusions = exclusions,
            preferredIcon = preferredIcon
        )

    private val _configuration = MutableStateFlow(currentConfig)
    override val configuration: Flow<Configuration> = _configuration.asStateFlow()

    override fun update(update: Configuration.() -> Configuration) {
        _configuration.value.let(update).let { newValue ->
            enabled = newValue.enabled
            host = newValue.host
            port = newValue.port
            durationSecs = newValue.durationSecs
            preferredIcon = newValue.preferredIcon
            exclusions = newValue.exclusions
            _configuration.value = currentConfig
        }
    }
}
