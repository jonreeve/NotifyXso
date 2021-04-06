package com.wasabicode.notificationstoxso.app.config

import android.content.Context
import com.wasabicode.notificationstoxso.server.types.MyNotification
import kotlin.reflect.KClass

interface Configuration {
    var enabled: Boolean
    var host: String
    var port: Int
    var durationSecs: Float
    var exclusions: Set<String>
    var preferredIcon: KClass<out MyNotification.Icon>
}

class SharedPrefsConfiguration(context: Context) : Configuration {
    private val sharedPrefs = context.getSharedPreferences("config", Context.MODE_PRIVATE)

    override var enabled by SharedPrefsDelegate.boolean(sharedPrefs)
    override var host by SharedPrefsDelegate.string(sharedPrefs, defaultValue = "192.168.1.")
    override var port by SharedPrefsDelegate.int(sharedPrefs, defaultValue = 43210)
    override var durationSecs by SharedPrefsDelegate.float(sharedPrefs, defaultValue = 2.0f)
    override var exclusions by SharedPrefsDelegate.stringSet(sharedPrefs, defaultValue = setOf(
        "Pebble Time",
        "Checking for new messages",
        "Checking for messages…",
        "Loading...",
        "USB debugging connected",
        "Charging this device via USB",
        "On sale from your wishlist",
    ))
    override var preferredIcon: KClass<out MyNotification.Icon> = MyNotification.Icon.Custom::class
}