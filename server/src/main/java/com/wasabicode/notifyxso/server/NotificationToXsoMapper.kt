package com.wasabicode.notifyxso.server

import com.wasabicode.notifyxso.server.types.MyNotification

fun MyNotification.toXsoMessage(): XsoMessage {
    return XsoMessage(
        title = titleRtf,
        content = contentRtf,
        icon = icon.xsoIcon,
        useBase64Icon = icon is MyNotification.Icon.Custom,
        timeout = durationSecs
    )
}

private val MyNotification.Icon.xsoIcon: String get() = when (this) {
    is MyNotification.Icon.Custom -> base64Icon
    is MyNotification.Icon.Default -> "default"
    is MyNotification.Icon.Error -> "error"
    is MyNotification.Icon.Warning -> "warning"
}
