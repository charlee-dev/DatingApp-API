package com.ajlabs.forevely.model.auth

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import org.bson.types.ObjectId

@GraphQLDescription(ResetPasswordResultDesc.MODEL)
data class ResetPasswordResult(
    @GraphQLDescription(ResetPasswordResultDesc.ID)
    val id: ObjectId,
    @GraphQLDescription(ResetPasswordResultDesc.SENT)
    val sent: Boolean,
)

object ResetPasswordResultDesc {
    const val MODEL = "Represents the result of reset password operation"
    const val ID = "Unique identifier for the reset password operation."
    const val SENT = "Whether the reset password email was sent or not."
}
