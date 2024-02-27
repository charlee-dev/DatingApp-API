package com.ajlabs.forevely.model

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@GraphQLDescription(MessageDesc.MODEL)
data class Message(
    @BsonId
    @GraphQLDescription(MessageDesc.ID)
    val id: ObjectId,
    @GraphQLDescription(MessageDesc.CONVERSATION_ID)
    val conversationId: ObjectId,
    @GraphQLDescription(MessageDesc.SENDER_ID)
    val senderId: ObjectId,
    @GraphQLDescription(MessageDesc.CONTENT)
    val content: String,
    @GraphQLDescription(MessageDesc.TIMESTAMP)
    val timestamp: String,
)

object MessageDesc {
    const val MODEL = "Represents a single message in a chat conversation."
    const val ID = "Unique identifier for the message."
    const val CONVERSATION_ID = "ID of the conversation that the message belongs to."
    const val SENDER_ID = "ID of the user who sent the message."
    const val CONTENT = "Content or body of the message."
    const val TIMESTAMP = "Timestamp when the message was sent or received."
}
