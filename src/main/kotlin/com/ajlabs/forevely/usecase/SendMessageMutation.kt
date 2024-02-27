package com.ajlabs.forevely.usecase

import com.ajlabs.forevely.domain.util.OBJECT_ID
import com.ajlabs.forevely.domain.util.withCurrentUser
import com.ajlabs.forevely.model.Conversation
import com.ajlabs.forevely.model.ConversationDesc
import com.ajlabs.forevely.model.Message
import com.ajlabs.forevely.model.user.PersonalDetails
import com.ajlabs.forevely.model.user.User
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Updates
import com.mongodb.client.result.InsertOneResult
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.flow.firstOrNull
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.koin.java.KoinJavaComponent
import java.util.logging.Level
import java.util.logging.Logger

object SendMessageMutation : Mutation {
    private val logger = Logger.getLogger(SendMessageMutation::class.java.name)
    private val database by KoinJavaComponent.inject<MongoDatabase>(MongoDatabase::class.java)
    private val messageCollection = database.getCollection<Message>(Message::class.java.simpleName)
    private val conversationCollection = database.getCollection<Conversation>(Conversation::class.java.simpleName)
    private val userCollection = database.getCollection<User>(User::class.java.simpleName)

    @Suppress("unused")
    @GraphQLDescription("Mutation to send a message")
    suspend fun sendMessage(
        dfe: DataFetchingEnvironment,
        @GraphQLDescription(SendMessageInputDesc.MODEL)
        input: SendMessageInput,
    ): MessageData? = dfe.withCurrentUser { senderId ->
        if (input.content.isBlank()) error("Message content cannot be blank")

        val message = Message(
            id = ObjectId(),
            conversationId = input.conversationId,
            senderId = senderId,
            content = input.content,
            timestamp = System.currentTimeMillis().toString(),
        )

        val insertOneResult: InsertOneResult = messageCollection.insertOne(message)
        return@withCurrentUser if (insertOneResult.wasAcknowledged()) {
            updateConversation(input.conversationId, message)

            val senderName = getSenderName(senderId) ?: error("Sender name not found")
            notifyFirebaseMessaging(senderId, senderName, message)

            MessageData(
                id = message.id,
                timestamp = message.timestamp,
            )
        } else {
            null
        }
    }

    private fun notifyFirebaseMessaging(
        senderId: ObjectId,
        senderName: String,
        message: Message,
    ) {
        val dataMessage = com.google.firebase.messaging.Message.builder()
            .putData("messageId", message.id.toString())
            .putData("conversationId", message.conversationId.toString())
            .putData("senderName", senderName)
            .putData("text", message.content)
            .putData("timestamp", message.timestamp)
            .setToken(senderId.toHexString())
            .build()

        logger.info("Sending message to Firebase: $dataMessage")
        try {
            FirebaseMessaging.getInstance().send(dataMessage)
        } catch (e: FirebaseMessagingException) {
            logger.log(Level.SEVERE, "Error sending message to Firebase: $e")
        }
    }

    private suspend fun getSenderName(id: ObjectId): String? {
        data class MatcherDetails(val name: String?)
        data class MatcherUser(val details: MatcherDetails)

        val filter = Filters.eq(OBJECT_ID, id)
        val projection = Projections.fields(
            Projections.excludeId(),
            Projections.include("${User::details.name}.${PersonalDetails::name.name}"),
        )
        val user = userCollection.find<MatcherUser>(filter).projection(projection).firstOrNull()
        return user?.details?.name
    }

    private suspend fun updateConversation(conversationId: ObjectId, message: Message): Boolean {
        val filter = Filters.eq(OBJECT_ID, conversationId)
        val bson = Updates.combine(
            Updates.addToSet(Conversation::messageIds.name, message.id),
            Updates.set(Conversation::lastMessageTimestamp.name, message.timestamp),
        )
        val updateResult = conversationCollection.updateOne(filter, bson)
        return updateResult.wasAcknowledged()
    }
}

@GraphQLDescription(SendMessageInputDesc.MODEL)
data class SendMessageInput(
    @GraphQLDescription(SendMessageInputDesc.CONVERSATION_ID)
    val conversationId: ObjectId,
    @GraphQLDescription(SendMessageInputDesc.CONTENT)
    val content: String,
)

private object SendMessageInputDesc {
    const val MODEL = "Input to send a message"
    const val CONVERSATION_ID = ConversationDesc.ID
    const val CONTENT = "The text of the message"
}

data class MessageData(
    @BsonId val id: ObjectId,
    val timestamp: String,
)
