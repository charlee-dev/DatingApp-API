package com.ajlabs.forevely.model.auth

import com.ajlabs.forevely.model.user.UserMinimal
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
@GraphQLDescription(AuthResponseDesc.MODEL)
data class AuthResponse(
    @GraphQLDescription(AuthResponseDesc.TOKEN)
    val token: String,
    @GraphQLDescription(AuthResponseDesc.USER_MINIMAL)
    val userMinimal: UserMinimal,
)

object AuthResponseDesc {
    const val MODEL = "A response to authentication."
    const val TOKEN = "The authentication token for the user."
    const val USER_MINIMAL = "Minimal information about the authenticated user."
}
