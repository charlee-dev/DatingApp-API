package com.ajlabs.forevely.usecase

import com.ajlabs.forevely.domain.util.OBJECT_ID
import com.ajlabs.forevely.domain.util.getInfo
import com.ajlabs.forevely.model.ConversationDesc
import com.ajlabs.forevely.model.Message
import com.ajlabs.forevely.model.MessageDesc
import com.ajlabs.forevely.model.PageInput
import com.ajlabs.forevely.model.PageInputDesc
import com.ajlabs.forevely.model.PagingInfo
import com.ajlabs.forevely.model.PagingInfoDesc
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.toList
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.koin.java.KoinJavaComponent

object GetMessagesPageQuery : Query {
    private val database by KoinJavaComponent.inject<MongoDatabase>(MongoDatabase::class.java)
    private val messageCollection = database.getCollection<Message>(Message::class.java.simpleName)

    @Suppress("unused")
    @GraphQLDescription("Query to get a page of messages")
    suspend fun getMessagesPage(
        @GraphQLDescription(ConversationDesc.ID)
        conversationId: ObjectId,
        @GraphQLDescription(PageInputDesc.MODEL)
        pageInput: PageInput,
    ): MessagesPage {
        pageInput.validate()

        val filters = Filters.eq(Message::conversationId.name, conversationId)
        val sort = Sorts.descending(OBJECT_ID)
        val projections = Projections.fields(
            // _id is included automatically unless explicitly excluded
            Projections.include(OBJECT_ID),
            Projections.include(Message::senderId.name),
            Projections.include(Message::content.name),
            Projections.include(Message::timestamp.name),
        )

        val skips = (pageInput.page - 1) * pageInput.size
        val messageItems = messageCollection.find<MessageItem>(filters)
            .sort(sort)
            .projection(projections)
            .skip(skips)
            .limit(pageInput.size)
            .partial(true)
            .toList()

        val total = messageCollection.countDocuments(filters).toInt()
        val pagingInfo = getInfo(total, pageInput)

        return MessagesPage(
            messages = messageItems,
            info = pagingInfo,
        )
    }
}

@GraphQLDescription(MessageDesc.MODEL)
data class MessageItem(
    @BsonId
    @GraphQLDescription(MessageDesc.ID)
    val id: ObjectId,
    @GraphQLDescription(MessageDesc.SENDER_ID)
    val senderId: ObjectId,
    @GraphQLDescription(MessageDesc.CONTENT)
    val content: String,
    @GraphQLDescription(MessageDesc.TIMESTAMP)
    val timestamp: String,
)

@GraphQLDescription(MessagesPageDesc.MODEL)
data class MessagesPage(
    @GraphQLDescription(MessagesPageDesc.MESSAGES)
    val messages: List<MessageItem>,
    @GraphQLDescription(MessagesPageDesc.INFO)
    val info: PagingInfo,
)

private object MessagesPageDesc {
    const val MODEL = "Represents a paginated list of Message objects"
    const val MESSAGES = "The list of Messages on this page"
    const val INFO = PagingInfoDesc.MODEL
}
