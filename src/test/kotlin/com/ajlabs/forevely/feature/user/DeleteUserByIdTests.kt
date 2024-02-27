package com.ajlabs.forevely.feature.user

import com.ajlabs.forevely.helpers.MongoTests
import com.ajlabs.forevely.mock.mockUsers
import com.ajlabs.forevely.model.user.User
import com.ajlabs.forevely.plugins.configureKoin
import com.expediagroup.graphql.server.types.GraphQLResponse
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be`
import org.amshove.kluent.`should contain`
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin

private const val OPERATION_NAME = "closeUserAccount"

class DeleteUserByIdTests : MongoTests() {
    private lateinit var database: MongoDatabase
    private lateinit var userCollection: MongoCollection<User>

    override fun beforeAll() {
        stopKoin()
        val koinApp = configureKoin(container.connectionString)
        database = koinApp.koin.get()
        userCollection = database.getCollection<User>(User::class.java.simpleName)
    }

    @BeforeEach
    fun beforeEach() {
        runBlocking {
            userCollection.insertMany(mockUsers)
        }
    }

    @AfterEach
    fun afterEach() {
        runBlocking {
            userCollection.drop()
        }
    }

    override fun afterAll() {
        runBlocking {
            database.drop()
        }
    }

    @Test
    fun `when user exists then calling delete should delete user`() =
        testDelete {
            data.toString() `should contain` "isDeleted=true"
            errors shouldBeEqualTo null
        }

    private fun testDelete(
        operationReturn: String = "userId isDeleted",
        assertion: suspend GraphQLResponse<*>.(HttpResponse) -> Unit,
    ) = test(
        doBefore = { userCollection.getUserId() },
        operation = "mutation $OPERATION_NAME { $OPERATION_NAME { $operationReturn } }",
        operationName = OPERATION_NAME,
        headers = { authorizationHeader(it) },
        variables = mapOf(),
    ) {
        errors `should be` null
        assertion(this, it)
    }
}
