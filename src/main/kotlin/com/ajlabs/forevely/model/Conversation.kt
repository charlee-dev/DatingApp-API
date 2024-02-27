package com.ajlabs.forevely.model

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@GraphQLDescription(ConversationDesc.MODEL)
data class Conversation(
    @BsonId
    @GraphQLDescription(ConversationDesc.ID)
    val id: ObjectId,
    @GraphQLDescription(ConversationDesc.PARTICIPANTS)
    val participantIds: List<ObjectId>,
    @GraphQLDescription(ConversationDesc.MESSAGES)
    val messageIds: List<ObjectId>,
    @GraphQLDescription(ConversationDesc.LAST_MESSAGE_TIMESTAMP)
    val lastMessageTimestamp: String?,
)

object ConversationDesc {
    const val MODEL = "Represents a single message in a chat conversation."
    const val ID = "Unique identifier for the message."
    const val PARTICIPANTS = "IDs of the participants in the conversation."
    const val MESSAGES = "Ids of the messages in the conversation."
    const val LAST_MESSAGE_TIMESTAMP = "Timestamp of the last message in the conversation."
}
