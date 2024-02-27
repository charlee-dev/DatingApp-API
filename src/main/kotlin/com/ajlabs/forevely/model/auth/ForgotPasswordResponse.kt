package com.ajlabs.forevely.model.auth

import com.ajlabs.forevely.model.user.UserDesc
import com.expediagroup.graphql.generator.annotations.GraphQLDescription

@GraphQLDescription(ForgotPasswordResponseDesc.MODEL)
data class ForgotPasswordResponse(
    @GraphQLDescription(ForgotPasswordResponseDesc.EMAIL)
    val email: String,
    @GraphQLDescription(ForgotPasswordResponseDesc.IS_FORGOT_PASSWORD_EMAIL_SENT)
    val isForgotPasswordEmailSent: Boolean,
)

object ForgotPasswordResponseDesc {
    const val MODEL = "Model for forgot password response"
    const val EMAIL = UserDesc.EMAIL
    const val IS_FORGOT_PASSWORD_EMAIL_SENT = "Indicates if forgot password email was sent"
}
