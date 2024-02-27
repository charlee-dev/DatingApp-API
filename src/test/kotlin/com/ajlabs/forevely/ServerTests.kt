package com.ajlabs.forevely

import com.ajlabs.forevely.helpers.MongoTests
import com.ajlabs.forevely.model.user.User
import com.ajlabs.forevely.plugins.configureKoin
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import kotlin.test.assertContains

class ServerTests : MongoTests() {
    private lateinit var database: MongoDatabase
    private lateinit var userCollection: MongoCollection<User>

    override fun beforeAll() {
        stopKoin()
        val koinApp = configureKoin(container.connectionString)
        database = koinApp.koin.get()

        userCollection = database.getCollection<User>(User::class.java.simpleName)
    }

    override fun afterAll() {
        runBlocking {
            database.drop()
        }
    }

    @Test
    fun `server should return Bad Request for invalid GET requests`() {
        testApplication {
            val response = client.get("/graphql")
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    fun `server should return Bad Request for invalid POST requests`() {
        testApplication {
            val response = client.post("/graphql")
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    fun `server should provide GraphiQL endpoint`() {
        testApplication {
            val response = client.get("/graphiql")
            assertEquals(HttpStatusCode.OK, response.status)

            val html = response.bodyAsText()
            assertContains(html, "var serverUrl = '/graphql';")
        }
    }
}
