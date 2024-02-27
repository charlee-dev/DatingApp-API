package com.ajlabs.forevely

import com.ajlabs.forevely.plugins.configureCORS
import com.ajlabs.forevely.plugins.configureGraphQL
import com.ajlabs.forevely.plugins.configureKoin
import com.ajlabs.forevely.plugins.configureLogging
import com.ajlabs.forevely.plugins.configureRouting
import com.ajlabs.forevely.plugins.configureWebSockets
import io.ktor.server.application.Application
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer

private val port = (System.getenv("PORT") ?: "8080").toInt()
private val mongoUri: String = System.getenv("MONGO_URI") ?: "mongodb://localhost:27017"

fun main() {
    embeddedServer(
        factory = CIO,
        port = port,
        watchPaths = listOf("/"),
        module = Application::applicationModule,
    ).start(wait = true)
}

fun Application.applicationModule() {
    configureKoin(mongoUri)
    configureCORS()
    configureLogging()
    configureWebSockets()
    configureGraphQL()
    configureRouting()
}
