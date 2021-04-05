package com.wasabicode.notificationstoxso.server

import kotlinx.serialization.Serializable

@Serializable
data class XsoMessage(
    val messageType: Int = 1,
    val index: Int = 0,
//    val volume: Float = 0.7f,
//    val audioPath: String = "default",
    val timeout: Float = 2f,
    val title: String,
    val content: String,
    val icon: String = "default",
//    val height: Int,
//    val opacity: Float,
    val useBase64Icon: Boolean = false,
    val sourceApp: String = "com.wasabicode.notificationstoxso"
)
