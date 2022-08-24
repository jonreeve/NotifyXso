package com.wasabicode.notifyxso.app.config

import android.content.Context

interface Configuration {
    var enabled: Boolean
    var server: Server
    var durationSecs: Float
    var exclusions: Set<String>
    var preferredIcon: PreferredIcon
}

data class ConfigurationVO(
    override var enabled: Boolean = false,
    override var server: Server = Server("192.168.1.", 43210),
    override var durationSecs: Float = 2f,
    override var exclusions: Set<String> = defaultExclusions,
    override var preferredIcon: PreferredIcon = PreferredIcon.Custom
) : Configuration {
    constructor(other: Configuration) : this(
        enabled = other.enabled,
        server = other.server,
        durationSecs = other.durationSecs,
        exclusions = other.exclusions,
        preferredIcon = other.preferredIcon
    )
}

private val defaultExclusions = setOf(
    "Pebble Time",
    "Checking for new messages",
    "Checking for messages…",
    "Loading...",
    "USB debugging connected",
    "Charging this device via USB",
    "On sale from your wishlist",
)
private val Defaults = ConfigurationVO()

class SharedPrefsConfiguration(context: Context) : Configuration {
    private val sharedPrefs = context.getSharedPreferences("config", Context.MODE_PRIVATE)

    override var enabled by SharedPrefsDelegate.boolean(sharedPrefs)
    override var server: Server
        get() = Server(host, port)
        set(value) {
            host = value.host
            port = value.port
        }
    override var durationSecs by SharedPrefsDelegate.float(sharedPrefs, defaultValue = Defaults.durationSecs)
    override var exclusions by SharedPrefsDelegate.stringSet(sharedPrefs, defaultValue = Defaults.exclusions)
    override var preferredIcon: PreferredIcon by SharedPrefsDelegate.int(sharedPrefs, defaultValue = Defaults.preferredIcon.ordinal)
        .mapped(
            get = { PreferredIcon.values()[it] },
            set = { it.ordinal }
        )
    private var host by SharedPrefsDelegate.string(sharedPrefs, defaultValue = Defaults.server.host)
    private var port by SharedPrefsDelegate.int(sharedPrefs, defaultValue = Defaults.server.port)
}
