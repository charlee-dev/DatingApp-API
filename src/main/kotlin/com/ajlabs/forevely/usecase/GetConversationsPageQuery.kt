package com.ajlabs.forevely.usecase

import com.ajlabs.forevely.domain.util.OBJECT_ID
import com.ajlabs.forevely.domain.util.getInfo
import com.ajlabs.forevely.domain.util.withCurrentUser
import com.ajlabs.forevely.model.Conversation
import com.ajlabs.forevely.model.Message
import com.ajlabs.forevely.model.PageInput
import com.ajlabs.forevely.model.PageInputDesc
import com.ajlabs.forevely.model.PagingInfo
import com.ajlabs.forevely.model.PagingInfoDesc
import com.ajlabs.forevely.model.user.PersonalDetails
import com.ajlabs.forevely.model.user.Profile
import com.ajlabs.forevely.model.user.User
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import com.mongodb.client.model.Aggregates
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Sorts
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.koin.java.KoinJavaComponent

object GetConversationsPageQuery : Query {
    private val database by KoinJavaComponent.inject<MongoDatabase>(MongoDatabase::class.java)
    private val userCollection = database.getCollection<User>(User::class.java.simpleName)
    private val conversationCollection = database.getCollection<Conversation>(Conversation::class.java.simpleName)
    private val messageCollection = database.getCollection<Message>(Message::class.java.simpleName)

    data class UserDetails(val name: String)
    data class UserProfile(val pictures: List<String>)
    data class MatcherMinimal(
        val details: UserDetails,
        val profile: UserProfile,
    )

    data class ConversationSample(
        @BsonId
        val id: ObjectId,
        val participantIds: List<ObjectId>,
        val lastMessageId: ObjectId?,
    )

    @Suppress("unused")
    @GraphQLDescription("Query to get a page of conversations")
    suspend fun getConversationsPage(
        dfe: DataFetchingEnvironment,
        @GraphQLDescription(PageInputDesc.MODEL)
        pageInput: PageInput,
    ): ConversationsPage = dfe.withCurrentUser { currentUserId ->
        pageInput.validate()

        val filters = Filters.`in`(Conversation::participantIds.name, listOf(currentUserId))
        val sort = Sorts.descending(Conversation::lastMessageTimestamp.name)
        val project = Projections.fields(
            listOf(
                // _id is included automatically unless explicitly excluded
                Projections.include(ConversationSample::participantIds.name),
                Projections.computed(
                    ConversationSample::lastMessageId.name,
                    Document("\$arrayElemAt", listOf("\$${Conversation::messageIds.name}", -1L)),
                ),
            ),
        )
        val skips = (pageInput.page - 1) * pageInput.size
        val skipsPositive = if (skips < 0) 0 else skips

        val conversationSamples = conversationCollection.aggregate<ConversationSample>(
            listOf(
                Aggregates.match(filters),
                Aggregates.sort(sort),
                Aggregates.project(project),
                Aggregates.skip(skipsPositive),
                Aggregates.limit(pageInput.size),
            ),
        ).toList()

        val conversationItems = conversationSamples.map { conversationSample ->
            val matcher = conversationSample.getMatcherMinimal(currentUserId)
            val lastMessage = conversationSample.lastMessageId?.let { getLastMessage(it) }
            ConversationItem(
                id = conversationSample.id,
                matcherName = matcher.details.name,
                picture = matcher.profile.pictures.firstOrNull(),
                lastMessage = lastMessage,
            )
        }

        val total = conversationCollection.countDocuments(filters).toInt()
        val pagingInfo = getInfo(total, pageInput)

        ConversationsPage(
            conversations = conversationItems,
            info = pagingInfo,
        )
    }

    private suspend fun ConversationSample.getMatcherMinimal(currentUserId: ObjectId): MatcherMinimal {
        val matcherId = participantIds.first { it != currentUserId }
        val filter = Filters.eq(OBJECT_ID, matcherId)
        val projection = Projections.fields(
            Projections.excludeId(),
            Projections.include("${User::details.name}.${PersonalDetails::name.name}"),
            Projections.include("${User::profile.name}.${Profile::pictures.name}"),
        )

        return userCollection.find<MatcherMinimal>(filter)
            .projection(projection)
            .first()
    }

    private suspend fun getLastMessage(messageId: ObjectId): LastMessage? {
        val filter = Filters.eq(OBJECT_ID, messageId)
        val projection = Projections.fields(
            Projections.excludeId(),
            Projections.include(Message::content.name, Message::timestamp.name),
        )
        return messageCollection.find<LastMessage>(filter).projection(projection).firstOrNull()
    }
}

@GraphQLDescription("Conversation item")
data class ConversationItem(
    @BsonId
    @GraphQLDescription("Conversation id")
    val id: ObjectId,
    @GraphQLDescription("List of participant names")
    val matcherName: String,
    @GraphQLDescription("Url of the matcher's profile picture")
    val picture: String?,
    @GraphQLDescription("Last message in the conversation")
    val lastMessage: LastMessage?,
)

@GraphQLDescription("Last message in a conversation")
data class LastMessage(
    @GraphQLDescription("Content of the last message")
    val content: String?,
    @GraphQLDescription("Timestamp of the last message")
    val timestamp: String?,
)

@GraphQLDescription("Page of conversations")
data class ConversationsPage(
    @GraphQLDescription("List of conversation items")
    val conversations: List<ConversationItem>,
    @GraphQLDescription(PagingInfoDesc.MODEL)
    val info: PagingInfo,
)
