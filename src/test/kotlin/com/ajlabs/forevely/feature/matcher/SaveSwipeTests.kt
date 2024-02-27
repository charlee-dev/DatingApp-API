package com.ajlabs.forevely.feature.matcher

import com.ajlabs.forevely.helpers.MongoTests
import com.ajlabs.forevely.mock.mockUsers
import com.ajlabs.forevely.mock.user1
import com.ajlabs.forevely.mock.user2
import com.ajlabs.forevely.model.user.SwipeType
import com.ajlabs.forevely.model.user.User
import com.ajlabs.forevely.plugins.configureKoin
import com.expediagroup.graphql.server.types.GraphQLResponse
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be`
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import kotlin.test.assertEquals

private const val OPERATION_NAME = "saveSwipe"

class SaveSwipeTests : MongoTests() {
    private lateinit var database: MongoDatabase
    private lateinit var userCollection: MongoCollection<User>

    override fun beforeAll() {
        stopKoin()
        val koinApp = configureKoin(container.connectionString)
        database = koinApp.koin.get()
        userCollection = database.getCollection<User>(User::class.java.simpleName)

        runBlocking {
            userCollection.insertMany(mockUsers)
        }
    }

    override fun afterAll() {
        runBlocking {
            database.drop()
        }
    }

    fun `when save like should return true`() = startTest(
        swipeType = SwipeType.LIKE,
        userId = user2.id.toString(),
    ) {
        data?.toString() shouldBeEqualTo "{${OPERATION_NAME}=true}"
    }

    @Test
    fun `when save dislike should return true`() = startTest(
        swipeType = SwipeType.DISLIKE,
        userId = user2.id.toString(),
    ) {
        data?.toString() shouldBeEqualTo "{${OPERATION_NAME}=true}"
    }

    @Test
    fun `when save like then User should have it in likes`() = startTest(
        swipeType = SwipeType.LIKE,
        userId = user2.id.toString(),
    ) {
        val user = userCollection.find(Filters.eq(User::email.name, user1.email)).first()
        assertEquals(user.swipes.likes.first(), user2.id)
    }

    @Test
    fun `when save dislike then User should have it in dislikes`() = startTest(
        swipeType = SwipeType.DISLIKE,
        userId = user2.id.toString(),
    ) {
        val user = userCollection.find(Filters.eq(User::email.name, user1.email)).first()
        assertEquals(user.swipes.dislikes.first(), user2.id)
    }

    private fun startTest(
        swipeType: SwipeType,
        userId: String,
        assertion: suspend GraphQLResponse<*>.(HttpResponse) -> Unit,
    ) = test(
        doBefore = { userCollection.getUserId() },
        operation = "mutation ${OPERATION_NAME}(\$swipeType: SwipeType!, \$id: ObjectId!)" +
            "{ ${OPERATION_NAME}(swipeType: \$swipeType, id: \$id) }",
        operationName = OPERATION_NAME,
        headers = { authorizationHeader(it) },
        variables = mapOf(
            "swipeType" to swipeType,
            "id" to userId,
        ),
    ) {
        errors `should be` null
        assertion(this, it)
    }
}
