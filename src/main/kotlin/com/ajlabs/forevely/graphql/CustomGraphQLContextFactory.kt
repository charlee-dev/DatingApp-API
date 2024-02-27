package com.ajlabs.forevely.graphql

import com.ajlabs.forevely.domain.JwtService
import com.ajlabs.forevely.domain.util.ErrorMessage
import com.expediagroup.graphql.generator.extensions.plus
import com.expediagroup.graphql.server.ktor.DefaultKtorGraphQLContextFactory
import graphql.GraphQLException
import io.ktor.http.HttpHeaders
import io.ktor.server.request.ApplicationRequest
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class CustomGraphQLContextFactory : DefaultKtorGraphQLContextFactory(), KoinComponent {
    private val jwtService: JwtService by inject()

    override suspend fun generateContext(request: ApplicationRequest): graphql.GraphQLContext =
        super.generateContext(request).plus(
            mutableMapOf(
                "Access-Control-Allow-Origin" to "*",
                "Access-Control-Allow-Methods" to "*",
                "Access-Control-Allow-Credentials" to "true",
                "Access-Control-Allow-Headers" to
                    "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since," +
                    "Cache-Control,Content-Type,Content-Range,Range",
                "Access-Control-Expose-Headers" to
                    "DNT,X-CustomHeader,Keep-Alive,User-Agent,X-Requested-With,If-Modified-Since," +
                    "Cache-Control,Content-Type,Content-Range,Range",
                "id" to request.getUserIdFromAuthHeader(),
            ),
        )

    private fun ApplicationRequest.getUserIdFromAuthHeader(): String {
        val token = headers[HttpHeaders.Authorization]
        val userId = token?.let { jwtService.verifyToken(it) }
        userId?.let { it.ifBlank { throw GraphQLException(ErrorMessage.TOKEN_FAILED_DECODE) } }
        return userId ?: ""
    }
}
