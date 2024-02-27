package com.ajlabs.forevely.usecase

import com.ajlabs.forevely.domain.InputValidator
import com.ajlabs.forevely.domain.JwtService
import com.ajlabs.forevely.domain.util.ErrorMessage
import com.ajlabs.forevely.model.auth.AuthInput
import com.ajlabs.forevely.model.auth.AuthInputDesc
import com.ajlabs.forevely.model.auth.AuthResponse
import com.ajlabs.forevely.model.user.User
import com.ajlabs.forevely.model.user.toMinimal
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import graphql.GraphQLException
import kotlinx.coroutines.flow.firstOrNull
import org.koin.java.KoinJavaComponent

object LoginMutation : Mutation {
    private val jwtService: JwtService by KoinJavaComponent.inject(JwtService::class.java)
    private val inputValidator: InputValidator by KoinJavaComponent.inject(InputValidator::class.java)
    private val database: MongoDatabase by KoinJavaComponent.inject(MongoDatabase::class.java)
    private val userCollection: MongoCollection<User> = database.getCollection<User>(User::class.java.simpleName)

    @GraphQLDescription("Signs in user with email and password, no header needed.")
    @Suppress("unused")
    suspend fun login(
        @GraphQLDescription(AuthInputDesc.MODEL)
        authInput: AuthInput,
    ): AuthResponse {
        if (inputValidator.isEmpty(authInput.email)) {
            throw GraphQLException(ErrorMessage.INVALID_EMAIL)
        }
        if (inputValidator.isEmpty(authInput.password)) {
            throw GraphQLException(ErrorMessage.INVALID_PASSWORD)
        }

        val user = userCollection.find(Filters.eq(User::email.name, authInput.email)).firstOrNull()
            ?: throw GraphQLException(ErrorMessage.NOT_FOUND)

        if (!jwtService.validatePasswordMatch(authInput.password, user.passwordHash, user.passwordSalt)) {
            throw GraphQLException(ErrorMessage.INVALID_PASSWORD)
        }

        val token = jwtService.generateToken(user.id)
        return AuthResponse(token, user.toMinimal())
    }
}
