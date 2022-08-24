package com.wasabicode.notifyxso.app.config

data class Server(
    val host: String,
    val port: Int
) {
    override fun toString(): String = "$host:$port"
}
