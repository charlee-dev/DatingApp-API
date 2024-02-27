package com.ajlabs.forevely.usecase

import com.ajlabs.forevely.domain.util.OBJECT_ID
import com.ajlabs.forevely.domain.util.withCurrentUser
import com.ajlabs.forevely.model.Conversation
import com.ajlabs.forevely.model.user.User
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.firstOrNull
import org.bson.types.ObjectId
import org.koin.java.KoinJavaComponent

object CreateConversationMutation : Mutation {
    private val database by KoinJavaComponent.inject<MongoDatabase>(MongoDatabase::class.java)
    private val conversationCollection: MongoCollection<Conversation> =
        database.getCollection<Conversation>(Conversation::class.java.simpleName)
    private val userCollection: MongoCollection<User> = database.getCollection<User>(User::class.java.simpleName)

    @Suppress("unused")
    @GraphQLDescription("Mutation to create a new conversation")
    suspend fun createConversation(
        dfe: DataFetchingEnvironment,
        @GraphQLDescription("ID of the user to create the conversation with")
        matcherId: ObjectId,
    ): Conversation = dfe.withCurrentUser { currentUserId ->
        if (currentUserId == matcherId) error("Cannot create conversation with self")

        val conversation = Conversation(
            id = ObjectId(),
            participantIds = listOf(currentUserId, matcherId),
            messageIds = emptyList(),
            lastMessageTimestamp = null,
        )

        val result = conversationCollection.insertOne(conversation)
        if (result.insertedId == null) error("Failed to create conversation")

        coroutineScope {
            conversation.participantIds.map { userId ->
                async { addToUserConversations(userId, conversation.id) }
            }.awaitAll()
        }

        val newConversation = conversationCollection.find(Filters.eq(OBJECT_ID, result.insertedId)).firstOrNull()
        newConversation ?: error("Failed to create conversation")
    }

    private suspend fun addToUserConversations(userId: ObjectId, conversationId: ObjectId) {
        userCollection.updateOne(
            Filters.eq(OBJECT_ID, userId),
            Updates.addToSet(User::conversationIds.name, conversationId),
        )
    }
}
