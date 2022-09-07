package com.wasabicode.notifyxso.app.config

data class Configuration(
    val enabled: Boolean = false,
    val host: String = "192.168.1.",
    val port: Int = 43210,
    val durationSecs: Float = 2f,
    val exclusions: Set<String> = defaultExclusions,
    val preferredIcon: PreferredIcon = PreferredIcon.Custom
) {
    companion object {
        val Defaults = Configuration()
    }
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
