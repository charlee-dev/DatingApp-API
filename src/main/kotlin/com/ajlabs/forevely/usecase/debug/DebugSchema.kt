package com.ajlabs.forevely.usecase.debug

import com.ajlabs.forevely.domain.util.OBJECT_ID
import com.ajlabs.forevely.domain.util.withCurrentUser
import com.ajlabs.forevely.model.Conversation
import com.ajlabs.forevely.model.Message
import com.ajlabs.forevely.model.user.Swipes
import com.ajlabs.forevely.model.user.User
import com.ajlabs.forevely.usecase.CreateConversationMutation
import com.ajlabs.forevely.usecase.SendMessageInput
import com.ajlabs.forevely.usecase.SendMessageMutation
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import com.expediagroup.graphql.server.operations.Query
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.flow.first
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.koin.java.KoinJavaComponent.inject
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class DebugSchema {
    class Queries : Query {
        private val database by inject<MongoDatabase>(MongoDatabase::class.java)
        private val userCollection = database.getCollection<User>(User::class.java.simpleName)
        private val messagesCollection = database.getCollection<Message>(Message::class.java.simpleName)
        private val conversationCollection = database.getCollection<Conversation>(Conversation::class.java.simpleName)

        @Suppress("unused")
        @GraphQLDescription("Generate users for testing purposes")
        suspend fun debugGenerateUsers(
            dfe: DataFetchingEnvironment,
            @GraphQLDescription("Target number of users to generate")
            target: Int = 200,
        ): GenerateUsersResult = dfe.withCurrentUser { currentUserId ->
            // Check how many users to generate
            val usersBefore = userCollection.countDocuments().toInt()
            val missingUsers = target - usersBefore
            if (missingUsers <= 0) {
                return@withCurrentUser GenerateUsersResult(
                    before = usersBefore,
                    inserted = missingUsers,
                    after = usersBefore,
                )
            }

            // generating users
            val usersToGenerate = getGeneratedUsers(missingUsers)
            val result = userCollection.insertMany(usersToGenerate)

            // Populate conversation with first user
            val firstUserId = usersToGenerate.first().id
            populateConversation(dfe, firstUserId)

            val totalInserted = result.insertedIds.size
            val usersAfter = userCollection.countDocuments().toInt()
            val generateUsersResult = GenerateUsersResult(
                before = usersBefore,
                after = usersAfter,
                inserted = totalInserted,
            )

            // Get users that liked current user
            val likes = usersToGenerate.map { it.id }
                .randomList((target * 0.2).toInt(), (target * 0.5).toInt())

            // Add those users to current user likes
            val updates = Updates.set("${User::swipes.name}.${Swipes::likes.name}", likes)

            userCollection.updateOne(Filters.eq(OBJECT_ID, currentUserId), updates)

            generateUsersResult
        }

        private suspend fun populateConversation(dfe: DataFetchingEnvironment, matcherId: ObjectId) {
            val conversation = CreateConversationMutation.createConversation(dfe, matcherId)
            val conversationId = conversation.id
            populateMessages(dfe, conversationId, matcherId)
        }

        private suspend fun populateMessages(
            dfe: DataFetchingEnvironment,
            conversationId: ObjectId,
            matcherId: ObjectId,
        ) {
            dfe.withCurrentUser { userId ->
                listOf(
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = matcherId,
                        content = "Hi",
                        timestamp = (System.currentTimeMillis() - 182.hours.inWholeMilliseconds - 130.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = matcherId,
                        content = "How are you?",
                        timestamp = (System.currentTimeMillis() - 182.hours.inWholeMilliseconds - 120.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = userId,
                        content = "I'm good, thanks. How about yourself?",
                        timestamp = (System.currentTimeMillis() - 123.hours.inWholeMilliseconds - 150.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = userId,
                        content = "Missed your previous message, sorry...",
                        timestamp = (System.currentTimeMillis() - 123.hours.inWholeMilliseconds - 130.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = userId,
                        content = "got caught up with work. What have you been up to?",
                        timestamp = (System.currentTimeMillis() - 123.hours.inWholeMilliseconds - 110.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = matcherId,
                        content = "I understand, no worries!",
                        timestamp = (System.currentTimeMillis() - 122.hours.inWholeMilliseconds - 140.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = matcherId,
                        content = "I've been busy too.",
                        timestamp = (System.currentTimeMillis() - 122.hours.inWholeMilliseconds - 130.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = matcherId,
                        content = "Just started a new project at work",
                        timestamp = (System.currentTimeMillis() - 122.hours.inWholeMilliseconds - 120.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = matcherId,
                        content = "it's quite challenging.",
                        timestamp = (System.currentTimeMillis() - 122.hours.inWholeMilliseconds - 110.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = matcherId,
                        content = "What about you? Any new developments on your end?",
                        timestamp = (System.currentTimeMillis() - 122.hours.inWholeMilliseconds - 10.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = userId,
                        content = "That sounds exciting! My days have been quite routine",
                        timestamp = (System.currentTimeMillis() - 122.hours.inWholeMilliseconds - 40.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = userId,
                        content = "just the usual office work.",
                        timestamp = (System.currentTimeMillis() - 122.hours.inWholeMilliseconds - 30.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = userId,
                        content = "Looking forward to the weekend though.",
                        timestamp = (System.currentTimeMillis() - 122.hours.inWholeMilliseconds - 20.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = userId,
                        content = "Any plans?",
                        timestamp = (System.currentTimeMillis() - 122.hours.inWholeMilliseconds - 10.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = matcherId,
                        content = "Yeah, the weekend is always a relief.",
                        timestamp = (System.currentTimeMillis() - 1220.minutes.inWholeMilliseconds - 30.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = matcherId,
                        content = "Thinking of going hiking.",
                        timestamp = (System.currentTimeMillis() - 1220.minutes.inWholeMilliseconds - 20.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = matcherId,
                        content = "Have you ever tried it?",
                        timestamp = (System.currentTimeMillis() - 1220.minutes.inWholeMilliseconds - 10.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = userId,
                        content = "I love hiking! Haven't been in a while though.",
                        timestamp = (System.currentTimeMillis() - 122.minutes.inWholeMilliseconds - 50.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = userId,
                        content = "Which trail are you considering?",
                        timestamp = (System.currentTimeMillis() - 122.minutes.inWholeMilliseconds - 30.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = matcherId,
                        content = "I was thinking about the Blue Ridge Trail.",
                        timestamp = (System.currentTimeMillis() - 120.minutes.inWholeMilliseconds - 30.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = matcherId,
                        content = "Ever been there?",
                        timestamp = (System.currentTimeMillis() - 120.minutes.inWholeMilliseconds - 20.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = matcherId,
                        content = "Ever been there?",
                        timestamp = (System.currentTimeMillis() - 120.minutes.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = userId,
                        content = "Yes, it's a fantastic choice!",
                        timestamp = (System.currentTimeMillis() - 1481.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = userId,
                        content = "You're going to have a great time.",
                        timestamp = (System.currentTimeMillis() - 1451.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = userId,
                        content = "You're going to have a great time.",
                        timestamp = (System.currentTimeMillis() - 141.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = matcherId,
                        content = "Can't wait!",
                        timestamp = (System.currentTimeMillis() - 130.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = matcherId,
                        content = "Maybe you could join? It could be fun.",
                        timestamp = (System.currentTimeMillis() - 100.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = userId,
                        content = "That sounds like a great idea.",
                        timestamp = (System.currentTimeMillis() - 70.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = userId,
                        content = "Let me check my schedule and I'll get back to you!",
                        timestamp = (System.currentTimeMillis() - 50.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = matcherId,
                        content = "Awesome, let me know.",
                        timestamp = (System.currentTimeMillis() - 20.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = matcherId,
                        content = "And hey, if you're free sometime next week, maybe we can grab a coffee and discuss more?",
                        timestamp = (System.currentTimeMillis() - 10.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = userId,
                        content = "I'd like that.",
                        timestamp = (System.currentTimeMillis() - 18.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = userId,
                        content = "I'll text you once I sort out my plans.",
                        timestamp = (System.currentTimeMillis() - 1.seconds.inWholeMilliseconds).toString(),
                    ),
                    Message(
                        id = ObjectId(),
                        conversationId = conversationId,
                        senderId = userId,
                        content = "Have a good hike!",
                        timestamp = System.currentTimeMillis().toString(),
                    ),
                ).forEach { message ->
                    val input = SendMessageInput(
                        conversationId = conversationId,
                        content = message.content,
                    )
                    SendMessageMutation.sendMessage(dfe, input)
                }
            }
        }

        @Suppress("unused")
        @GraphQLDescription("Delete all users")
        suspend fun debugDeleteAllUsers(): Boolean {
            userCollection.drop()
            messagesCollection.drop()
            conversationCollection.drop()
            return userCollection.countDocuments() == 0L
        }

        @Suppress("unused")
        @GraphQLDescription("Delete all generated users")
        suspend fun debugDeleteGeneratedUsers(
            dfe: DataFetchingEnvironment,
        ): Boolean = dfe.withCurrentUser { currentUserId ->
            userCollection.deleteMany(Filters.ne(OBJECT_ID, currentUserId))

            // Remove swipes from current user
            val filters = Filters.eq(OBJECT_ID, currentUserId)
            val updates = Updates.set(User::swipes.name, null)
            val updateResult = userCollection.updateOne(filters, updates)
            updateResult.wasAcknowledged()
        }
    }

    class Mutations : Mutation {
        private val database by inject<MongoDatabase>(MongoDatabase::class.java)
        private val userCollection = database.getCollection<User>(User::class.java.simpleName)

        @Suppress("unused")
        @GraphQLDescription("Toggle current user premium users")
        suspend fun debugSetIsPremium(
            dfe: DataFetchingEnvironment,
            isPremium: Boolean,
        ): UserPremium = dfe.withCurrentUser {
            val filter = Filters.eq(OBJECT_ID, it)

            userCollection.updateOne(filter, Updates.set(User::isPremium.name, isPremium))

            val projection = Projections.fields(
                Projections.include(UserPremium::isPremium.name),
                Projections.excludeId(),
            )
            userCollection.find<UserPremium>(filter).projection(projection).first()
        }
    }
}

data class UserPremium(
    @BsonId val id: Int? = null,
    val isPremium: Boolean,
)
