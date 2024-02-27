package com.ajlabs.forevely.model.user

import com.expediagroup.graphql.generator.annotations.GraphQLDescription

internal const val INTERESTS_COMPLETED_SIZE_MIN = 2

@GraphQLDescription(ProfileDesc.MODEL)
data class Profile(
    @GraphQLDescription(ProfileDesc.BIO)
    val bio: String? = null,
    @GraphQLDescription(ProfileDesc.PICTURES)
    val pictures: List<String> = emptyList(),
    @GraphQLDescription(ProfileDesc.INTERESTS)
    val interests: Interests? = null,
    @GraphQLDescription(ProfileDesc.ABOUT_ME)
    val aboutMe: AboutMe = AboutMe(),
    @GraphQLDescription(ProfileDesc.OPEN_QUESTION)
    val openQuestion: OpenQuestion? = null,
)

data class ProfileUpdateInput(
    @GraphQLDescription(ProfileDesc.PICTURES)
    val pictures: List<String>? = null,
    @GraphQLDescription(ProfileDesc.BIO)
    val bio: String? = null,
    @GraphQLDescription(ProfileDesc.INTERESTS)
    val interests: Interests? = null,
    @GraphQLDescription(ProfileDesc.ABOUT_ME)
    val aboutMe: AboutMe? = null,
    @GraphQLDescription(ProfileDesc.OPEN_QUESTION)
    val openQuestion: OpenQuestion? = null,
)

object ProfileDesc {
    const val MODEL = "Represents a user's profile."
    const val PICTURES = "List of profile pictures."
    const val BIO = "Short bio or description of the user."
    const val INTERESTS =
        "List of interests. Need to have minimum $INTERESTS_COMPLETED_SIZE_MIN interests to mark this as completed."
    const val ABOUT_ME = "List of about me details."
    const val OPEN_QUESTION = "An open question for other users"
}
