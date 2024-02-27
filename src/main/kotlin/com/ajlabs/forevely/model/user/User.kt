package com.ajlabs.forevely.model.user

import com.ajlabs.forevely.domain.InputValidator
import com.ajlabs.forevely.domain.util.ErrorMessage
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.generator.annotations.GraphQLIgnore
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import kotlin.reflect.full.memberProperties

@GraphQLDescription(UserDesc.MODEL)
data class User(
    @BsonId
    @GraphQLDescription(UserDesc.ID)
    val id: ObjectId,
    @GraphQLDescription(UserDesc.EMAIL)
    val email: String,
    @GraphQLIgnore
    var passwordHash: ByteArray,
    @GraphQLIgnore
    var passwordSalt: ByteArray,
    @GraphQLDescription(UserDesc.PROFILE)
    val profile: Profile,
    @GraphQLDescription(UserDesc.PERSONAL_DETAILS)
    val details: PersonalDetails,
    @GraphQLDescription(VerificationDesc.MODEL)
    val verification: Verification,
    @GraphQLDescription(UserDesc.CONVERSATIONS)
    val conversationIds: List<ObjectId>,
    @GraphQLDescription(UserDesc.PROFILE_COMPLETION)
    val profileCompletion: Double,
    @GraphQLDescription(UserDesc.IS_PREMIUM)
    val isPremium: Boolean, // For now just a Boolean but will need expanding to different premium types and active boosts
    @GraphQLDescription(SwipesDesc.MODEL)
    val swipes: Swipes,
    @GraphQLDescription(UserDesc.UPDATED_AT)
    var updatedAt: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as User

        if (id != other.id) return false
        if (email != other.email) return false
        if (!passwordHash.contentEquals(other.passwordHash)) return false
        if (!passwordSalt.contentEquals(other.passwordSalt)) return false
        if (profile != other.profile) return false
        if (details != other.details) return false
        if (verification != other.verification) return false
        if (conversationIds != other.conversationIds) return false
        if (profileCompletion != other.profileCompletion) return false
        if (updatedAt != other.updatedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + passwordHash.contentHashCode()
        result = 31 * result + passwordSalt.contentHashCode()
        result = 31 * result + profile.hashCode()
        result = 31 * result + details.hashCode()
        result = 31 * result + verification.hashCode()
        result = 31 * result + conversationIds.hashCode()
        result = 31 * result + updatedAt.hashCode()
        return result
    }
}

@GraphQLDescription(UserDesc.INPUT_UPDATE)
data class UserUpdateInput(
    @GraphQLDescription(UserDesc.EMAIL)
    val email: String? = null,
    @GraphQLDescription(UserDesc.PASSWORD)
    val password: String? = null,
)

object UserDesc {
    const val MODEL = "Represents a user object"
    const val INPUT_UPDATE = "Represents a user update input object"
    const val ID = "Unique identifier of the user."
    const val EMAIL = "Email of the user."
    const val PASSWORD = """
User password. Special characters prohibited @£€#¢∞§¶•ªº\${'$'}%^&*()_+="
Invalid format will throw error: '${ErrorMessage.INVALID_PASSWORD}'"

Regex: ${InputValidator.REGEX_PASSWORD}
"""
    const val PROFILE = "User profile information"
    const val PERSONAL_DETAILS = "Personal details of the user."
    const val CONVERSATIONS = "List of the conversations IDs of a specific user"
    const val PROFILE_COMPLETION = "Percentage of profile completion"
    const val IS_PREMIUM = "Whether the user is premium or not"
    const val UPDATED_AT = "Last modification date in 'Long' as a String"
}

fun User.checkProfileCompletion(): Double {
    val profileCompletion = profile.completions()
    val profileAboutMeCompletion = profile.aboutMe.completions()
    val personalDetailsCompletion = details.completions()
    val verifiedCompletions = verification.completions()

    val allCompletions = profileAboutMeCompletion + personalDetailsCompletion + verifiedCompletions + profileCompletion
    val total = allCompletions.size
    return allCompletions.count { completed -> completed } * 100.0 / total
}

private fun AboutMe.completions(): List<Boolean> {
    val completions = listOf(
        languages.isNotEmpty(),
        height != null,
        fitness != null,
        education != null,
        smoking != null,
        drinking != null,
        personality != null,
        loveLanguage != null,
        children != null,
        relationship != null,
        diet != null,
        pets != null,
        zodiac != null,
        politics != null,
        religion != null,
    )
    if (completions.size != (this::class.memberProperties.size)) {
        error("${this::class.simpleName} completion list has missing properties")
    }
    return completions
}

private fun PersonalDetails.completions(): List<Boolean> {
    val completions = listOf(
        name != null,
        phone != null,
        dob != null,
        age != null,
        job != null,
        education != null,
        gender != null,
        currentLoc != null,
        liveLoc != null,
        birthLoc != null,
    )
    if (completions.size != this::class.memberProperties.size) {
        error("${this::class.simpleName} completion list has missing properties")
    }
    return completions
}

private fun Verification.completions(): List<Boolean> {
    val completions = listOf(
        photoVerified,
        emailVerified,
        phoneVerified,
    )
    if (completions.size != this::class.memberProperties.size) {
        error("${this::class.simpleName} completion list has missing properties")
    }
    return completions
}

private fun Profile.completions(): List<Boolean> {
    return listOf(
        !bio.isNullOrEmpty(),
        pictures.isNotEmpty(),
        (interests != null && interests.size() >= INTERESTS_COMPLETED_SIZE_MIN),
        openQuestion != null,
    )
}
