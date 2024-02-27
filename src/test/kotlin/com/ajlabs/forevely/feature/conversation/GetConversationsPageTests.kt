package com.ajlabs.forevely.feature.conversation

import com.ajlabs.forevely.helpers.MongoTests
import com.ajlabs.forevely.mock.mockConversations
import com.ajlabs.forevely.mock.mockUsers
import com.ajlabs.forevely.mock.user1
import com.ajlabs.forevely.mock.user2
import com.ajlabs.forevely.mock.user3
import com.ajlabs.forevely.model.Conversation
import com.ajlabs.forevely.model.PageInput
import com.ajlabs.forevely.model.user.User
import com.ajlabs.forevely.plugins.configureKoin
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin

private const val OPERATION_NAME = "getConversationsPage"

class GetConversationsPageTests : MongoTests() {
    private lateinit var database: MongoDatabase
    private lateinit var userCollection: MongoCollection<User>

    override fun beforeAll() {
        stopKoin()
        val koinApp = configureKoin(container.connectionString)
        database = koinApp.koin.get()
        userCollection = database.getCollection<User>(User::class.java.simpleName)

        runBlocking {
            userCollection.insertMany(mockUsers)

            val conversationCollection = database.getCollection<Conversation>(Conversation::class.java.simpleName)
            conversationCollection.insertMany(mockConversations)
        }
    }

    override fun afterAll() {
        runBlocking {
            database.drop()
        }
    }

    @Test
    fun `should return current user conversations`() = test(
        doBefore = { userCollection.find(Filters.eq("email", user1.email)).first().id },
        operationName = OPERATION_NAME,
        operation = "query $OPERATION_NAME(\$pageInput: PageInput!) { $OPERATION_NAME(pageInput: \$pageInput) " +
            "{ conversations { participantIds }}}",
        headers = { authorizationHeader(it) },
        variables = mapOf("pageInput" to PageInput(0, 10)),
    ) {
        errors shouldBeEqualTo null
        data.toString() shouldBeEqualTo "{$OPERATION_NAME={conversations=[" +
            "{participantIds=[${user1.id}, ${user3.id}]}, " +
            "{participantIds=[${user1.id}, ${user2.id}]}]}}"
    }
}
