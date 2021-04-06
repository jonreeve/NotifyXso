package com.wasabicode.notificationstoxso.app

import com.wasabicode.notificationstoxso.server.types.MyNotification
import kotlin.reflect.KClass

class Configuration {
    var enabled: Boolean = false
    var host: String = "192.168.1.71"
    var port: Int = 43210
    var durationSecs: Float = 2.0f
    var exclusions: List<String> = listOf(
        "Pebble Time",
        "Checking for new messages",
        "Checking for messages…",
        "Loading...",
        "USB debugging connected",
        "Charging this device via USB",
        "On sale from your wishlist",
    )
    var preferredIcon: KClass<out MyNotification.Icon> = MyNotification.Icon.Custom::class
}