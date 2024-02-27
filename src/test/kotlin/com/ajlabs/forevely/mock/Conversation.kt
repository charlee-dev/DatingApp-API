package com.ajlabs.forevely.mock

import com.ajlabs.forevely.model.Conversation
import org.bson.types.ObjectId

val conversation1 = Conversation(
    id = ObjectId(),
    participantIds = listOf(user1.id, user2.id),
    messageIds = listOf(ObjectId(), ObjectId()),
    lastMessageTimestamp = "0L",
)

val conversation2 = Conversation(
    id = ObjectId(),
    participantIds = listOf(user2.id, user3.id),
    messageIds = listOf(ObjectId(), ObjectId()),
    lastMessageTimestamp = "1L",
)

val conversation3 = Conversation(
    id = ObjectId(),
    participantIds = listOf(user1.id, user3.id),
    messageIds = emptyList(),
    lastMessageTimestamp = "2L",
)

val conversation4 = Conversation(
    id = ObjectId(),
    participantIds = listOf(user2.id, user3.id),
    messageIds = emptyList(),
    lastMessageTimestamp = "4L",
)

val mockConversations = listOf(conversation1, conversation2, conversation3, conversation4)
