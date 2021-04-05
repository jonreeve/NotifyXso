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
            val notification = call.receive<MyNotification>()
            log.debug("Notification received: $notification")
            val xsoMessage = XsoMessage(
                title = notification.titleRtf,
                content = notification.contentRtf,
                timeout = notification.durationSecs
            )
            val xsoJson = json.encodeToString(xsoMessage)
            log.debug("Converted to XsoMessage: $xsoMessage, JSON: $xsoJson")
            val byteArray = xsoJson.toByteArray()
            val sendResult = async(Dispatchers.IO) {
                log.debug("Sending to $localHost:${config.xsOverlayPort}")
                socket.send(DatagramPacket(byteArray, byteArray.size, localHost, config.xsOverlayPort))
            }
            sendResult.await()
            call.respond(HttpStatusCode.Created)
        }
    }
}