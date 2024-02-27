package com.ajlabs.forevely.model.auth

import com.expediagroup.graphql.generator.annotations.GraphQLDescription

@GraphQLDescription(AuthInputDesc.MODEL)
data class AuthInput(
    @GraphQLDescription(AuthInputDesc.EMAIL)
    val email: String,
    @GraphQLDescription(AuthInputDesc.PASSWORD)
    val password: String,
)

object AuthInputDesc {
    const val MODEL = "Input data for authentication."
    const val EMAIL = "The email address for the user."
    const val PASSWORD = "The password for the user."
}
