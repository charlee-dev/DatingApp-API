package com.ajlabs.forevely.plugins

import com.expediagroup.graphql.server.ktor.graphQLGetRoute
import com.expediagroup.graphql.server.ktor.graphQLPostRoute
import com.expediagroup.graphql.server.ktor.graphQLSDLRoute
import com.expediagroup.graphql.server.ktor.graphQLSubscriptionsRoute
import com.expediagroup.graphql.server.ktor.graphiQLRoute
import io.ktor.http.ContentType
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get

fun Application.configureRouting() {
    install(Routing) {
        graphQLPostRoute()
        graphQLGetRoute()
        graphQLSubscriptionsRoute()
        graphiQLRoute()
        graphQLSDLRoute()

        get("apollo") {
            call.respondText(
                buildPlaygroundHtml("graphql-apollo.html"),
                ContentType.Text.Html,
            )
        }
        get("playground") {
            call.respondText(
                buildPlaygroundHtml("graphql-playground.html"),
                ContentType.Text.Html,
            )
        }
    }
}

private fun buildPlaygroundHtml(
    resource: String,
    graphQLEndpoint: String = "graphql",
    subscriptionsEndpoint: String = "subscriptions",
) = Application::class.java.classLoader.getResource(resource)?.readText()
    ?.replace("\${graphQLEndpoint}", graphQLEndpoint)
    ?.replace("\${subscriptionsEndpoint}", subscriptionsEndpoint)
    ?: error("$resource cannot be found in the classpath")
