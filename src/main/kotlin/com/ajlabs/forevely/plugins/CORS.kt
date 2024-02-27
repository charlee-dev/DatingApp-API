package com.ajlabs.forevely.plugins

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Options)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.SecWebSocketAccept)
        allowHeader(HttpHeaders.ContentType)
        allowHeadersPrefixed("x-apollo")
        allowHeadersPrefixed("X-Requested-With")
        allowHeader(HttpHeaders.AccessControlAllowOrigin)
        allowSameOrigin = true
        allowCredentials = true
        anyHost()
    }
}
