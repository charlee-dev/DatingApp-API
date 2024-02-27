package com.ajlabs.forevely.feature.auth

import com.ajlabs.forevely.domain.util.ErrorMessage
import com.ajlabs.forevely.helpers.MongoTests
import com.ajlabs.forevely.mock.PASS_1
import com.ajlabs.forevely.mock.PASS_2
import com.ajlabs.forevely.mock.user1
import com.ajlabs.forevely.mock.user2
import com.ajlabs.forevely.model.auth.AuthInput
import com.ajlabs.forevely.model.user.User
import com.ajlabs.forevely.plugins.configureKoin
import com.expediagroup.graphql.server.types.GraphQLResponse
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin

private const val OPERATION_NAME = "login"

class LoginTests : MongoTests() {
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
            userCollection.insertOne(user1)
        }
    }

    @AfterEach
    fun afterEach() {
        runBlocking {
            userCollection.drop()
        }
    }

    override fun afterAll() = runBlocking {
        database.drop()
    }

    @Test
    fun `when login in with valid credentials should return data with AuthResponse`() = testLogin(
        variables = mapOf(
            "authInput" to AuthInput(
                email = user1.email,
                password = PASS_1,
            ),
        ),
    ) {
        data.toString() shouldContain "userMinimal={email=${user1.email}}"
        errors shouldBeEqualTo null
    }

    @Test
    fun `when signing with not existing user should throw error`() = testLogin(
        variables = mapOf(
            "authInput" to AuthInput(
                email = user2.email,
                password = PASS_2,
            ),
        ),
    ) {
        it.status shouldBeEqualTo HttpStatusCode.OK
        data shouldBeEqualTo null
        errors?.first()?.message.toString() shouldContain ErrorMessage.NOT_FOUND
    }

    @Test
    fun `when login in with blank email should return error EMAIL_BLANK`() = testLogin(
        variables = mapOf(
            "authInput" to AuthInput(
                email = "",
                password = PASS_1,
            ),
        ),
    ) {
        it.status shouldBeEqualTo HttpStatusCode.OK
        data shouldBeEqualTo null
        errors?.first()?.message.toString() shouldContain ErrorMessage.INVALID_EMAIL
    }

    @Test
    fun `when login in with blank password should return error PASSWORD_BLANK`() = testLogin(
        variables = mapOf(
            "authInput" to AuthInput(
                email = user1.email,
                password = "",
            ),
        ),
    ) {
        it.status shouldBeEqualTo HttpStatusCode.OK
        data shouldBeEqualTo null
        errors?.first()?.message.toString() shouldContain ErrorMessage.INVALID_PASSWORD
    }

    @Test
    fun `when login in with invalid email should return error NOT_FOUND`() = testLogin(
        variables = mapOf(
            "authInput" to AuthInput(
                email = "invalidEmail",
                password = PASS_1,
            ),
        ),
    ) {
        it.status shouldBeEqualTo HttpStatusCode.OK
        data shouldBeEqualTo null
        errors?.first()?.message.toString() shouldContain ErrorMessage.NOT_FOUND
    }


    private fun testLogin(
        operationReturns: String = "email",
        variables: Map<String, Any>,
        assertion: suspend GraphQLResponse<*>.(HttpResponse) -> Unit,
    ) = test(
        operation = "mutation $OPERATION_NAME(\$authInput: AuthInput!) { $OPERATION_NAME(authInput: \$authInput) " +
            "{ token userMinimal { $operationReturns }}}",
        operationName = OPERATION_NAME,
        variables = variables,
        assertion = assertion,
    )
}
