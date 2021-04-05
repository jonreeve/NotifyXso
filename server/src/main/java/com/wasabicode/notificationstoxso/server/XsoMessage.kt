package com.wasabicode.notificationstoxso.server

import kotlinx.serialization.Serializable

@Serializable
data class XsoMessage(
    val messageType: Int = 1, // notification
    val index: Int = 0, // unused, only for media player messageType
//    val volume: Float = 0.7f,
//    val audioPath: String = "default",
    val timeout: Float = 2f,
    val title: String,
    val content: String,
    val icon: String = "default",
    val useBase64Icon: Boolean = false,
//    val height: Int,
//    val opacity: Float,
    val sourceApp: String = "com.wasabicode.notificationstoxso"
) {
    override fun toString(): String {
        val iconString = if (icon.length > 20) "(long text)" else "'$icon'"
        return "XsoMessage(messageType=$messageType, index=$index, timeout=$timeout, title='$title', content='$content', icon=$iconString, useBase64Icon=$useBase64Icon, sourceApp='$sourceApp')"
    }
}

