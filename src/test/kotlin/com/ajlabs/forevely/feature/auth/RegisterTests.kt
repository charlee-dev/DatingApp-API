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
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin

private const val OPERATION_NAME = "register"

class RegisterTests : MongoTests() {
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

    override fun afterAll() {
        runBlocking {
            database.drop()
        }
    }

    @Test
    fun `when register user with email and password then user should be registered`() = testRegister(
        variables = mapOf(
            "authInput" to AuthInput(
                email = user2.email,
                password = PASS_2,
            ),
        ),
    ) {
        data.toString() shouldContain "userMinimal={email=${user2.email}}"
        errors shouldBeEqualTo null
    }

    @Test
    fun `when register but User already exists should return error EMAIL_IN_USE`() = testRegister(
        variables = mapOf(
            "authInput" to AuthInput(
                email = user1.email,
                password = PASS_1,
            ),
        ),
    ) {
        data shouldBeEqualTo null
        errors?.first()?.message.toString() shouldContain ErrorMessage.EMAIL_IN_USE
    }

    @Test
    fun `when register with blank email should return error INVALID_EMAIL`() = testRegister(
        variables = mapOf(
            "authInput" to AuthInput(
                email = "",
                password = PASS_1,
            ),
        ),
    ) {
        data shouldBeEqualTo null
        errors?.first()?.message.toString() shouldContain ErrorMessage.INVALID_EMAIL
    }

    @Test
    fun `when register email has no @ should return error INVALID_EMAIL`() = testRegister(
        variables = mapOf(
            "authInput" to AuthInput(
                password = PASS_1,
                email = "email",
            ),
        ),
    ) {
        data shouldBeEqualTo null
        errors?.first()?.message.toString() shouldContain ErrorMessage.INVALID_EMAIL
    }

    @Test
    fun `when register email has invalid Regex should return error INVALID_EMAIL`() {
        listOf(
            "email@",
            "email@.",
            "email@.com",
            "email@.com.",
            "email@.com.com",
            ".email@.com.com",
            "email()*@.com.com",
            "email@%*.com.com",
            "email..email@.com.com",
            "email.@gmail.com",
            "email@",
        ).forEach { email ->
            println("\n\uD83D\uDCE7 Email: $email \n")
            testRegister(
                variables = mapOf(
                    "authInput" to AuthInput(
                        email = email,
                        password = PASS_1,
                    ),
                ),
            ) {
                data shouldBeEqualTo null
                errors?.first()?.message.toString() shouldContain ErrorMessage.INVALID_EMAIL
            }
        }
    }

    @Test
    fun `when register password has invalid Regex should return error INVALID_PASSWORD`() {
        listOf(
            "password",
            "Password",
            "password1",
            "Password!",
            "Pass.word!",
            "Pass word!",
            "Pass1",
        ).forEach { password ->
            println("\uD83D\uDD11 Password: $password \n")
            testRegister(
                variables = mapOf(
                    "authInput" to AuthInput(
                        email = user1.email,
                        password = password,
                    ),
                ),
            ) {
                data shouldBeEqualTo null
                errors?.first()?.message.toString() shouldContain ErrorMessage.INVALID_PASSWORD
            }
        }
    }

    @Test
    fun `when register with blank password should return error PASSWORD_BLANK`() = testRegister(
        variables = mapOf(
            "authInput" to AuthInput(
                email = user1.email,
                password = "",
            ),
        ),
    ) {
        data shouldBeEqualTo null
        errors?.first()?.message.toString() shouldContain ErrorMessage.INVALID_PASSWORD
    }

    private fun testRegister(
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
