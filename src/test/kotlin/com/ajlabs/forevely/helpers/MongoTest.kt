package com.ajlabs.forevely.helpers

import com.ajlabs.forevely.mock.user1
import com.ajlabs.forevely.model.user.User
import com.ajlabs.forevely.plugins.configureGraphQL
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.expediagroup.graphql.server.ktor.graphQLGetRoute
import com.expediagroup.graphql.server.ktor.graphQLPostRoute
import com.expediagroup.graphql.server.ktor.graphQLSDLRoute
import com.expediagroup.graphql.server.ktor.graphQLSubscriptionsRoute
import com.expediagroup.graphql.server.ktor.graphiQLRoute
import com.expediagroup.graphql.server.types.GraphQLRequest
import com.expediagroup.graphql.server.types.GraphQLResponse
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMessageBuilder
import io.ktor.http.contentType
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.request.path
import io.ktor.server.routing.Routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.flow.first
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.koin.test.KoinTest
import org.slf4j.event.Level
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration

private const val MONGO_CONTAINER = "mongo:4.0.28" // Match version in docker-compose.yml
private const val PORT = 27017

@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class MongoTests : KoinTest {
    protected val container = MongoDBContainer(MONGO_CONTAINER)
        .waitingFor(Wait.forListeningPort())
        .withStartupTimeout(Duration.ofSeconds(30))
        .withExposedPorts(PORT)
        .withReuse(true)

    @BeforeAll
    fun setupAll() {
        container.start()
        beforeAll()
    }

    @AfterAll
    fun teardownAll() {
        afterAll()
        container.stop()
    }

    abstract fun beforeAll()
    abstract fun afterAll()

    fun test(
        doBefore: suspend () -> ObjectId? = { null },
        operation: String,
        operationName: String,
        headers: suspend HttpMessageBuilder.(currentUserId: ObjectId) -> Unit = {},
        variables: Map<String, Any?>,
        assertion: suspend GraphQLResponse<*>.(HttpResponse) -> Unit,
    ) {
        testApplication {
            setupApplication()
            val currentUserId = doBefore()
            val response = getHttpClient().post("graphql") {
                contentType(ContentType.Application.Json)
                currentUserId?.let { headers(currentUserId) }
                setBody(
                    GraphQLRequest(
                        query = operation,
                        operationName = operationName,
                        variables = variables,
                    ),
                )
            }
            println("📧 Response: ${response.bodyAsText()}")
            val graphQlResponse = response.body<GraphQLResponse<*>>()
            println("📧 graphQlResponse: $graphQlResponse")
            assertion(graphQlResponse, response)
        }
    }

    fun HttpMessageBuilder.authorizationHeader(id: ObjectId) {
        val token = JWT.create()
            .withIssuer(id.toHexString())
            .withClaim("userId", id.toHexString())
            .sign(Algorithm.HMAC256("secret"))
            .let { "Bearer $it" }
        return header(HttpHeaders.Authorization, token)
    }

    context(ApplicationTestBuilder)
    fun setupApplication() {
        application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                jackson {
                    enable(SerializationFeature.INDENT_OUTPUT)
                }
            }
            install(CallLogging) {
                level = Level.INFO
                filter { call -> call.request.path().startsWith("/") }
            }
        }
    }

    context(ApplicationTestBuilder)
    fun getHttpClient(): HttpClient {
        return createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                jackson {
                    enable(SerializationFeature.INDENT_OUTPUT)
                    setDefaultLeniency(true)
                    registerModules(
                        KotlinModule(
                            nullToEmptyMap = true,
                            nullToEmptyCollection = true,
                        ),
                    )
                }
            }
        }
    }

    suspend fun MongoCollection<User>.getUserId(user: User = user1): ObjectId {
        return find(Filters.eq(User::email.name, user.email)).first().id
    }
}

@Suppress("unused") // Used in test resources/application.conf
fun Application.testGraphQLModule() {
    configureGraphQL()
    install(io.ktor.server.websocket.WebSockets)
    install(Routing) {
        graphQLGetRoute()
        graphQLPostRoute()
        graphQLSubscriptionsRoute()
        graphQLSDLRoute()
        graphiQLRoute()
    }
}
