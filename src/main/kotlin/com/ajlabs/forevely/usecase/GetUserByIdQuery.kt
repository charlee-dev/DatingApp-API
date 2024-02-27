package com.ajlabs.forevely.usecase

import com.ajlabs.forevely.domain.util.OBJECT_ID
import com.ajlabs.forevely.model.user.User
import com.ajlabs.forevely.model.user.UserDesc
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.firstOrNull
import org.bson.types.ObjectId
import org.koin.java.KoinJavaComponent

object GetUserByIdQuery : Query {
    val database by KoinJavaComponent.inject<MongoDatabase>(MongoDatabase::class.java)
    val userCollection = database.getCollection<User>(User::class.java.simpleName)

    @GraphQLDescription("Get user by ID")
    @Suppress("unused")
    suspend fun getUserById(
        @GraphQLDescription(UserDesc.ID)
        id: ObjectId,
    ): User {
        return userCollection.find(Filters.eq(OBJECT_ID, id)).firstOrNull()
            ?: error("User not found")
    }
}
