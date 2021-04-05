package com.wasabicode.notificationstoxso.server.types

import kotlinx.serialization.Serializable

@Serializable
data class MyNotification(
    val titleRtf: String,
    val contentRtf: String,
    val icon: Icon = Icon.Default,
    val durationSecs: Float
) {
    @Serializable
    sealed class Icon {
        object Default: Icon()
        object Warning: Icon()
        object Error: Icon()
        data class Custom(val base64Icon: String)
    }
}
