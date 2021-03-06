package com.wasabicode.notifyxso.app.config

import com.wasabicode.notifyxso.server.types.MyNotification

enum class PreferredIcon {
    Default,
    Warning,
    Error,
    Custom;

    fun toNotificationIcon(customIconBitmapBase64: String?): MyNotification.Icon = when (this) {
        Default -> MyNotification.Icon.Default
        Warning -> MyNotification.Icon.Warning
        Error -> MyNotification.Icon.Error
        Custom -> customIconBitmapBase64?.let { MyNotification.Icon.Custom(it) } ?: MyNotification.Icon.Default
    }
}