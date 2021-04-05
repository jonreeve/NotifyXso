package com.wasabicode.notificationstoxso.server

import com.wasabicode.notificationstoxso.server.types.MyNotification
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.put
import io.ktor.routing.routing
import io.ktor.serialization.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    val localHost = InetAddress.getLocalHost()
    val socket = DatagramSocket()
    val config = Configuration()
    val json = Json {
        encodeDefaults = true
    }

    install(ContentNegotiation) {
        json(json)
    }

    routing {
        put("/") {
            try {
                val notification = call.receive<MyNotification>()
                log.debug("Notification received: $notification")
                val sendResult = async(Dispatchers.IO) {
                    val xsoMessage = notification.toXsoMessage()
                    log.debug("Converted to XsoMessage: $xsoMessage")
                    val byteArray = json.encodeToString(xsoMessage).toByteArray()
                    if (byteArray.size > MAX_DATAGRAM_SIZE)
                        error("This message is too big (${byteArray.size} bytes, should be < $MAX_DATAGRAM_SIZE bytes)")

                    log.debug("Sending to $localHost:${config.xsOverlayPort}")
                    socket.send(DatagramPacket(byteArray, byteArray.size, localHost, config.xsOverlayPort))
                }
                sendResult.await()
                call.respond(HttpStatusCode.Created)
            } catch (throwable: Throwable) {
                log.error("Error handling request", throwable)
                throw throwable
            }
        }
    }
}

private fun MyNotification.toXsoMessage(): XsoMessage {
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

const val MAX_DATAGRAM_SIZE = 65_507