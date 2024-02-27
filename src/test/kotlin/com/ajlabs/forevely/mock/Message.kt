package com.ajlabs.forevely.mock

import com.ajlabs.forevely.model.Message
import org.bson.types.ObjectId

val message1 = Message(
    id = ObjectId(),
    conversationId = conversation1.id,
    senderId = user1.id,
    content = "Hello1",
    timestamp = "0L",
)

val message2 = Message(
    id = ObjectId(),
    conversationId = conversation2.id,
    senderId = user2.id,
    content = "Hello2",
    timestamp = "1L",
)

val message3 = Message(
    id = ObjectId(),
    conversationId = conversation3.id,
    senderId = user1.id,
    content = "Hello3",
    timestamp = "2L",
)

val message4 = Message(
    id = ObjectId(),
    conversationId = conversation4.id,
    senderId = user2.id,
    content = "Hello4",
    timestamp = "3L",
)

val mockMessages = listOf(message1, message2, message3, message4)
