package com.ajlabs.forevely.model.user

import com.expediagroup.graphql.generator.annotations.GraphQLDescription

@GraphQLDescription(VerificationDesc.MODEL)
data class Verification(
    @GraphQLDescription(VerificationDesc.PHOTO_VERIFIED)
    val photoVerified: Boolean = false,
    @GraphQLDescription(VerificationDesc.EMAIL_VERIFIED)
    val emailVerified: Boolean = false,
    @GraphQLDescription(VerificationDesc.PHONE_NUMBER_VERIFIED)
    val phoneVerified: Boolean = false,
)

object VerificationDesc {
    const val MODEL = "Contains the verification state of a user"
    const val PHOTO_VERIFIED = "Flag holding the photo verification state"
    const val EMAIL_VERIFIED = "Flag holding the email verification state"
    const val PHONE_NUMBER_VERIFIED = "Flag holding the phone number verification state"
}
