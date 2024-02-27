package com.ajlabs.forevely.usecase

import com.ajlabs.forevely.domain.InputValidator
import com.ajlabs.forevely.domain.util.ErrorMessage
import com.ajlabs.forevely.domain.util.OBJECT_ID
import com.ajlabs.forevely.domain.util.encrypt
import com.ajlabs.forevely.domain.util.generateSalt
import com.ajlabs.forevely.domain.util.withCurrentUser
import com.ajlabs.forevely.model.user.PersonalDetailsUpdateInput
import com.ajlabs.forevely.model.user.ProfileUpdateInput
import com.ajlabs.forevely.model.user.User
import com.ajlabs.forevely.model.user.UserDesc
import com.ajlabs.forevely.model.user.UserUpdateInput
import com.ajlabs.forevely.model.user.checkProfileCompletion
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import graphql.GraphQLException
import graphql.schema.DataFetchingEnvironment
import io.ktor.util.date.getTimeMillis
import kotlinx.coroutines.flow.firstOrNull
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.koin.java.KoinJavaComponent
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

object UpdateUserMutation : Mutation {
    private val database by KoinJavaComponent.inject<MongoDatabase>(MongoDatabase::class.java)
    private val userCollection = database.getCollection<User>(User::class.java.simpleName)
    private val inputValidator by KoinJavaComponent.inject<InputValidator>(InputValidator::class.java)

    @GraphQLDescription("Mutation to update a user's profile by ID (for use in an admin panel)")
    @Suppress("unused")
    suspend fun updateUser(
        dfe: DataFetchingEnvironment,
        @GraphQLDescription(UserDesc.INPUT_UPDATE)
        userUpdateInput: UserUpdateInput? = null,
        @GraphQLDescription(UserDesc.PROFILE)
        profileUpdateInput: ProfileUpdateInput? = null,
        @GraphQLDescription(UserDesc.PERSONAL_DETAILS)
        personalDetailsUpdateInput: PersonalDetailsUpdateInput? = null,
    ): User = dfe.withCurrentUser { executorId ->
        userUpdateInput?.email?.let {
            val user = userCollection.find(Filters.eq(User::email.name, it)).firstOrNull()
            if (user != null) error(ErrorMessage.EMAIL_IN_USE)

            if (!inputValidator.isValidEmail(it)) throw GraphQLException(ErrorMessage.INVALID_EMAIL)
        }

        var passwordSalt: ByteArray? = null
        val hashedPass = userUpdateInput?.password?.let { password ->
            if (!inputValidator.isValidPassword(password)) throw GraphQLException(ErrorMessage.INVALID_PASSWORD)
            passwordSalt = generateSalt()
            passwordSalt?.let { password.encrypt(it) }
        }


        val user = userCollection.find(Filters.eq(OBJECT_ID, executorId)).firstOrNull()
            ?: error("User not found")


        val newCompletion = user.checkProfileCompletion()
        val profileCompletion: Double? = if (newCompletion != user.profileCompletion) newCompletion else null

        val newIsPremium = false // Not implemented yet so using false for now
        val isPremium: Boolean? = if (newIsPremium != user.isPremium) newIsPremium else null

        updateUser(
            id = executorId,
            email = userUpdateInput?.email,
            hashedPass = hashedPass,
            passwordSalt = passwordSalt,
            profileUpdateInput = profileUpdateInput,
            personalDetailsUpdateInput = personalDetailsUpdateInput,
            profileCompletion = profileCompletion,
            isPremium = isPremium,
        )
    }

    private suspend fun updateUser(
        id: ObjectId,
        email: String?,
        hashedPass: ByteArray?,
        passwordSalt: ByteArray?,
        profileUpdateInput: ProfileUpdateInput?,
        personalDetailsUpdateInput: PersonalDetailsUpdateInput?,
        profileCompletion: Double?,
        isPremium: Boolean?,
    ): User {
        val bson = mutableListOf<Bson>().apply {
            email?.let { add(Updates.set(User::email.name, email)) }

            hashedPass?.let { pass ->
                passwordSalt?.let { salt ->
                    add(Updates.set(User::passwordHash.name, pass))
                    add(Updates.set(User::passwordSalt.name, salt))
                }
            }

            profileUpdateInput?.let {
                updateValuesIfNotNull(it, ProfileUpdateInput::class, User::profile.name)
            }

            personalDetailsUpdateInput?.let {
                updateValuesIfNotNull(it, PersonalDetailsUpdateInput::class, User::details.name)
            }

            profileCompletion?.let {
                add(Updates.set(User::profileCompletion.name, profileCompletion))
            }

            isPremium?.let {
                add(Updates.set(User::isPremium.name, isPremium))
            }
        }
        if (bson.isEmpty()) {
            throw GraphQLException("Nothing to update.")
        }
        bson.add(Updates.set(User::updatedAt.name, getTimeMillis().toString()))

        val updates = Updates.combine(bson)

        val filter = Filters.eq(OBJECT_ID, id)
        userCollection.updateOne(filter, updates)

        var updatedUser = userCollection.find(Filters.eq(OBJECT_ID, id)).firstOrNull()
            ?: throw GraphQLException(ErrorMessage.NOT_FOUND + " $id")

        val updatedUserCompletion = updatedUser.checkProfileCompletion()
        updatedUser = updatedUser.copy(profileCompletion = updatedUserCompletion)

        return updatedUser
    }

    private fun <T : Any> MutableList<Bson>.updateValuesIfNotNull(
        newValue: T,
        kClass: KClass<T>,
        fieldPrefix: String,
    ) {
        kClass.memberProperties.forEach { property ->
            val value = property.get(newValue)
            if (value != null) {
                add(Updates.set("$fieldPrefix.${property.name}", value))
            }
        }
    }
}
