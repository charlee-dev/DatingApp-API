package com.ajlabs.forevely.feature.conversation

import com.ajlabs.forevely.helpers.MongoTests
import com.ajlabs.forevely.mock.user1
import com.ajlabs.forevely.mock.user2
import com.ajlabs.forevely.model.user.User
import com.ajlabs.forevely.plugins.configureKoin
import com.expediagroup.graphql.server.types.GraphQLResponse
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be`
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin

private const val OPERATION_NAME = "createConversation"

class CreateConversationTests : MongoTests() {
    private lateinit var database: MongoDatabase
    private lateinit var userCollection: MongoCollection<User>

    override fun beforeAll() {
        stopKoin()
        val koinApp = configureKoin(container.connectionString)
        database = koinApp.koin.get()
        userCollection = database.getCollection<User>(User::class.java.simpleName)

        runBlocking {
            userCollection.insertOne(user1)
        }
    }

    override fun afterAll() {
        runBlocking {
            database.drop()
        }
    }

    @Test
    fun `create conversation should have participants`() = startTest {
        data.toString() shouldBeEqualTo
            "{$OPERATION_NAME={participantIds=[${user1.id.toHexString()}, ${user2.id.toHexString()}]}}"
    }

    @Test
    fun `create conversation should have empty messages`() = startTest(
        operationReturns = "messageIds",
    ) {
        data.toString() shouldBeEqualTo "{$OPERATION_NAME={messageIds=[]}}"
    }

    private fun startTest(
        operationReturns: String = "participantIds",
        assertion: suspend GraphQLResponse<*>.(HttpResponse) -> Unit,
    ) = test(
        doBefore = { userCollection.getUserId() },
        operation = "mutation $OPERATION_NAME(\$participantId: ObjectId!) { " +
            "$OPERATION_NAME(participantId: \$participantId) { $operationReturns }}",
        operationName = OPERATION_NAME,
        headers = { authorizationHeader(it) },
        variables = mapOf(
            "participantId" to user2.id.toHexString(),
        ),
    ) {
        errors `should be` null
        assertion(this, it)
    }
}
