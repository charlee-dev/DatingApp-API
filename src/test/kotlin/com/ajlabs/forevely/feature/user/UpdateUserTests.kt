package com.ajlabs.forevely.feature.user

import com.ajlabs.forevely.domain.util.ErrorMessage
import com.ajlabs.forevely.domain.util.OBJECT_ID
import com.ajlabs.forevely.helpers.MongoTests
import com.ajlabs.forevely.mock.user1
import com.ajlabs.forevely.mock.user2
import com.ajlabs.forevely.model.Language
import com.ajlabs.forevely.model.user.AboutMe
import com.ajlabs.forevely.model.user.Children
import com.ajlabs.forevely.model.user.Creative
import com.ajlabs.forevely.model.user.Culinary
import com.ajlabs.forevely.model.user.Diet
import com.ajlabs.forevely.model.user.Drinking
import com.ajlabs.forevely.model.user.Education
import com.ajlabs.forevely.model.user.Fitness
import com.ajlabs.forevely.model.user.Gender
import com.ajlabs.forevely.model.user.Interests
import com.ajlabs.forevely.model.user.Leisure
import com.ajlabs.forevely.model.user.Location
import com.ajlabs.forevely.model.user.LoveLanguage
import com.ajlabs.forevely.model.user.Mind
import com.ajlabs.forevely.model.user.Nature
import com.ajlabs.forevely.model.user.OpenQuestion
import com.ajlabs.forevely.model.user.PersonalDetailsUpdateInput
import com.ajlabs.forevely.model.user.Personality
import com.ajlabs.forevely.model.user.Pet
import com.ajlabs.forevely.model.user.Politics
import com.ajlabs.forevely.model.user.ProfileUpdateInput
import com.ajlabs.forevely.model.user.Relationship
import com.ajlabs.forevely.model.user.Religion
import com.ajlabs.forevely.model.user.SimpleLoc
import com.ajlabs.forevely.model.user.Smoking
import com.ajlabs.forevely.model.user.Social
import com.ajlabs.forevely.model.user.Sports
import com.ajlabs.forevely.model.user.Technology
import com.ajlabs.forevely.model.user.User
import com.ajlabs.forevely.model.user.UserUpdateInput
import com.ajlabs.forevely.model.user.Zodiac
import com.ajlabs.forevely.model.user.checkProfileCompletion
import com.ajlabs.forevely.plugins.configureKoin
import com.expediagroup.graphql.server.types.GraphQLResponse
import com.mongodb.client.model.Filters
import com.mongodb.client.model.geojson.Point
import com.mongodb.client.model.geojson.Position
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val OPERATION_NAME = "updateUser"

class UpdateUserTests : MongoTests() {
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

    private data class UpdateUser<T>(val updateUser: T)

    @Test
    fun `should be able to update only the user bio within a user profile`() {
        val newBio = "newBio"

        testUpdateUser(
            mutationReturns = "email",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    bio = newBio,
                ),
            ),
        ) {
            errors shouldBeEqualTo null

            val user = userCollection.find<User>(Filters.eq(User::email.name, user1.email)).first()
            newBio shouldBeEqualTo user.profile.bio
        }
    }

    @Test
    fun `should be able to update several fields within a user`() {
        val newName = "newName"
        val newBio = "newBio"
        val newEmail = "new@email.com"

        testUpdateUser(
            mutationReturns = "email",
            variables = mapOf(
                "userUpdateInput" to UserUpdateInput(
                    email = newEmail,
                ),
                "profileUpdateInput" to ProfileUpdateInput(
                    bio = newBio,
                ),
                "personalDetailsUpdateInput" to PersonalDetailsUpdateInput(
                    name = newName,
                ),
            ),
        ) {
            errors shouldBeEqualTo null

            val user = userCollection.find<User>(Filters.eq(User::email.name, newEmail)).first()
            newEmail shouldBeEqualTo user.email
            newBio shouldBeEqualTo user.profile.bio
            newName shouldBeEqualTo user.details.name
        }
    }

    @Test
    fun `should be able to update only the email within a user profile`() {
        val newEmail = "new@email.com"

        testUpdateUser(
            mutationReturns = "email",
            variables = mapOf(
                "userUpdateInput" to UserUpdateInput(
                    email = newEmail,
                ),
            ),
        ) {
            val users = userCollection.find<User>(Filters.empty()).toList()
            println("Users: $users")
            val user = userCollection.find<User>(Filters.eq(User::email.name, newEmail)).first()
            assertEquals(newEmail, user.email)
        }
    }

    @Test
    fun `update email to new email should update successfully`() {
        val newEmail = "new@email.com"
        testUpdateUser(
            mutationReturns = "email",
            variables = mapOf(
                "userUpdateInput" to UserUpdateInput(
                    email = newEmail,
                ),
            ),
        ) {
            data?.toString() shouldBeEqualTo "{${OPERATION_NAME}={email=$newEmail}}"
        }
    }

    @Test
    fun `update email to existing email should throw`() {
        testUpdateUser(
            mutationReturns = "email",
            variables = mapOf(
                "userUpdateInput" to UserUpdateInput(
                    email = user2.email,
                ),
            ),
        ) {
            errors.toString() shouldContain ErrorMessage.EMAIL_IN_USE
        }
    }

    @Test
    fun `populating empty field should update user and profile completion should be higher then before`() {
        var completionBefore = 0.0
        testUpdateUser(
            doBefore = {
                val userId = userCollection.getUserId()
                var user = userCollection.find<User>(Filters.eq(OBJECT_ID, userId)).first()
                completionBefore = user.checkProfileCompletion()
                println("completionBefore: $completionBefore")
                user = user.copy(profileCompletion = completionBefore)
                println("user before: $user")
                userCollection.replaceOne(Filters.eq(OBJECT_ID, userId), user)
                userId
            },
            mutationReturns = "profileCompletion",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    bio = "newBio",
                ),
            ),
        ) {
            data class Completion(val profileCompletion: Double)

            val completion = it.body<GraphQLResponse<UpdateUser<Completion>>>()
            assertTrue(completionBefore < (completion.data?.updateUser?.profileCompletion ?: 0.0))
        }
    }

    @Test
    fun `updating with valid password should update password`() {
        testUpdateUser(
            mutationReturns = "email",
            variables = mapOf(
                "userUpdateInput" to UserUpdateInput(
                    password = "Ab1cdef2",
                ),
            ),
        ) {
            errors shouldBeEqualTo null
        }
    }

    @Test
    fun `updating with invalid password should throw INVALID_PASSWORD`() {
        testUpdateUser(
            mutationReturns = "email",
            variables = mapOf(
                "userUpdateInput" to UserUpdateInput(
                    password = "digits",
                ),
            ),
        ) {
            errors.toString() shouldContain ErrorMessage.INVALID_PASSWORD
        }
    }

    @Test
    fun `updating pictures should update successfully`() {
        val newPictures = listOf("url1", "url2")
        testUpdateUser(
            mutationReturns = "profile { pictures }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    pictures = newPictures,
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={pictures=$newPictures}}}"
        }
    }

    @Test
    fun `updating bio should update successfully`() {
        val newBio = "newBio"
        testUpdateUser(
            mutationReturns = "profile { bio }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    bio = newBio,
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={bio=$newBio}}}"
        }
    }

    @Test
    fun `updating interests sports should update successfully`() {
        val new = listOf(Sports.GOLF, Sports.SKIING)
        testUpdateUser(
            mutationReturns = "profile { interests { sports } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    interests = Interests(
                        sports = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={interests={sports=$new}}}}"
        }
    }

    @Test
    fun `updating interests culinaries should update successfully`() {
        val new = listOf(Culinary.BAKING, Culinary.COOKING)
        testUpdateUser(
            mutationReturns = "profile { interests { culinaries } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    interests = Interests(
                        culinaries = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={interests={culinaries=$new}}}}"
        }
    }

    @Test
    fun `updating interests creativities should update successfully`() {
        val new = listOf(Creative.DANCING, Creative.DRAWING)
        testUpdateUser(
            mutationReturns = "profile { interests { creativities } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    interests = Interests(
                        creativities = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={interests={creativities=$new}}}}"
        }
    }

    @Test
    fun `updating interests leisures should update successfully`() {
        val new = listOf(Leisure.BIRD_WATCHING, Leisure.ANTIQUING)
        testUpdateUser(
            mutationReturns = "profile { interests { leisures } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    interests = Interests(
                        leisures = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={interests={leisures=$new}}}}"
        }
    }

    @Test
    fun `updating interests socials should update successfully`() {
        val new = listOf(Social.BOOK_CLUB, Social.SOCIAL_ACTIVISM)
        testUpdateUser(
            mutationReturns = "profile { interests { socials } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    interests = Interests(
                        socials = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={interests={socials=$new}}}}"
        }
    }

    @Test
    fun `updating interests technologies should update successfully`() {
        val new = listOf(Technology.BLOGGING, Technology.PODCASTING)
        testUpdateUser(
            mutationReturns = "profile { interests { technologies } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    interests = Interests(
                        technologies = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={interests={technologies=$new}}}}"
        }
    }

    @Test
    fun `updating interests natures should update successfully`() {
        val new = listOf(Nature.CAMPING, Nature.GARDENING)
        testUpdateUser(
            mutationReturns = "profile { interests { natures } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    interests = Interests(
                        natures = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={interests={natures=$new}}}}"
        }
    }

    @Test
    fun `updating interests minds should update successfully`() {
        val new = listOf(Mind.MINDFULNESS, Mind.MEDITATION)
        testUpdateUser(
            mutationReturns = "profile { interests { minds } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    interests = Interests(
                        minds = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={interests={minds=$new}}}}"
        }
    }

    @Test
    fun `updating OpenQuestion should update successfully`() {
        val new = OpenQuestion.ACHIEVEMENT
        testUpdateUser(
            mutationReturns = "profile { openQuestion }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    openQuestion = new,
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={openQuestion=$new}}}"
        }
    }

    @Test
    fun `updating personal details name should update successfully`() {
        val newName = "newName"
        testUpdateUser(
            mutationReturns = "details { name }",
            variables = mapOf(
                "personalDetailsUpdateInput" to PersonalDetailsUpdateInput(
                    name = newName,
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={details={name=$newName}}}"
        }
    }

    @Test
    fun `updating personal details phone should update successfully`() {
        val newPhone = "123456789"
        testUpdateUser(
            mutationReturns = "details { phone }",
            variables = mapOf(
                "personalDetailsUpdateInput" to PersonalDetailsUpdateInput(
                    phone = newPhone,
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={details={phone=$newPhone}}}"
        }
    }

    @Test
    fun `updating personal details dob should update successfully`() {
        val newDob = "01/01/2000"
        testUpdateUser(
            mutationReturns = "details { dob }",
            variables = mapOf(
                "personalDetailsUpdateInput" to PersonalDetailsUpdateInput(
                    dob = newDob,
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={details={dob=$newDob}}}"
        }
    }

    @Test
    fun `updating personal details job should update successfully`() {
        val newJob = "newJob"
        testUpdateUser(
            mutationReturns = "details { job }",
            variables = mapOf(
                "personalDetailsUpdateInput" to PersonalDetailsUpdateInput(
                    job = newJob,
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={details={job=$newJob}}}"
        }
    }

    @Test
    fun `updating personal details education should update successfully`() {
        val newEducation = Education.BACHELORS
        testUpdateUser(
            mutationReturns = "details { education }",
            variables = mapOf(
                "personalDetailsUpdateInput" to PersonalDetailsUpdateInput(
                    education = newEducation,
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={details={education=$newEducation}}}"
        }
    }

    @Test
    fun `updating personal details gender should update successfully`() {
        val newGender = Gender.NON_BINARY
        testUpdateUser(
            mutationReturns = "details { gender }",
            variables = mapOf(
                "personalDetailsUpdateInput" to PersonalDetailsUpdateInput(
                    gender = newGender,
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={details={gender=$newGender}}}"
        }
    }

    @Test
    fun `updating personal details currentLoc should update successfully`() {
        val newCurrentLoc = Location(
            geo = Point(Position(listOf(1.0, 2.0))),
            city = "newCity",
            state = "newState",
            country = "newCountry",
        )
        testUpdateUser(
            mutationReturns = "details { currentLoc { geo city state country } }",
            variables = mapOf(
                "personalDetailsUpdateInput" to mapOf(
                    "currentLoc" to mapOf(
                        "geo" to mapOf(
                            "coordinates" to listOf(1.0, 2.0),
                        ),
                        "city" to "newCity",
                        "state" to "newState",
                        "country" to "newCountry",
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={details={currentLoc=" +
                "{geo={coordinates=${newCurrentLoc.geo?.coordinates?.values}}, city=${newCurrentLoc.city}, " +
                "state=${newCurrentLoc.state}, country=${newCurrentLoc.country}}}}}"
        }
    }

    @Test
    fun `updating personal details liveLoc should update successfully`() {
        val newSimpleLoc = SimpleLoc(
            city = "newCity",
            country = "newCountry",
        )
        testUpdateUser(
            mutationReturns = "details { liveLoc { city country } }",
            variables = mapOf(
                "personalDetailsUpdateInput" to PersonalDetailsUpdateInput(
                    liveLoc = newSimpleLoc,
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={details={liveLoc=" +
                "{city=${newSimpleLoc.city}, country=${newSimpleLoc.country}}}}}"
        }
    }

    @Test
    fun `updating personal details birthLoc should update successfully`() {
        val newSimpleLoc = SimpleLoc(
            city = "newCity",
            country = "newCountry",
        )
        testUpdateUser(
            mutationReturns = "details { birthLoc { city country } }",
            variables = mapOf(
                "personalDetailsUpdateInput" to PersonalDetailsUpdateInput(
                    birthLoc = newSimpleLoc,
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={details={birthLoc=" +
                "{city=${newSimpleLoc.city}, country=${newSimpleLoc.country}}}}}"
        }
    }

    @Test
    fun `updating profile aboutMe languages should update successfully`() {
        val new = listOf(Language.Spanish, Language.Polish)
        testUpdateUser(
            mutationReturns = "profile { aboutMe { languages } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    aboutMe = AboutMe(
                        languages = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={aboutMe={languages=$new}}}}"
        }
    }

    @Test
    fun `updating profile aboutMe height should update successfully`() {
        val new = 180
        testUpdateUser(
            mutationReturns = "profile { aboutMe { height } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    aboutMe = AboutMe(
                        height = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={aboutMe={height=$new}}}}"
        }
    }

    @Test
    fun `updating profile aboutMe fitness should update successfully`() {
        val new = Fitness.EVERYDAY
        testUpdateUser(
            mutationReturns = "profile { aboutMe { fitness } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    aboutMe = AboutMe(
                        fitness = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={aboutMe={fitness=$new}}}}"
        }
    }

    @Test
    fun `updating profile aboutMe education should update successfully`() {
        val new = Education.BACHELORS
        testUpdateUser(
            mutationReturns = "profile { aboutMe { education } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    aboutMe = AboutMe(
                        education = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={aboutMe={education=$new}}}}"
        }
    }

    @Test
    fun `updating profile aboutMe smoking should update successfully`() {
        val new = Smoking.REGULAR_SMOKER
        testUpdateUser(
            mutationReturns = "profile { aboutMe { smoking } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    aboutMe = AboutMe(
                        smoking = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={aboutMe={smoking=$new}}}}"
        }
    }

    @Test
    fun `updating profile aboutMe drinking should update successfully`() {
        val new = Drinking.REGULAR_DRINKER
        testUpdateUser(
            mutationReturns = "profile { aboutMe { drinking } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    aboutMe = AboutMe(
                        drinking = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={aboutMe={drinking=$new}}}}"
        }
    }

    @Test
    fun `updating profile aboutMe personality should update successfully`() {
        val new = Personality.ENFJ
        testUpdateUser(
            mutationReturns = "profile { aboutMe { personality } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    aboutMe = AboutMe(
                        personality = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={aboutMe={personality=$new}}}}"
        }
    }

    @Test
    fun `updating profile aboutMe loveLanguage should update successfully`() {
        val new = LoveLanguage.ACTS_OF_SERVICE
        testUpdateUser(
            mutationReturns = "profile { aboutMe { loveLanguage } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    aboutMe = AboutMe(
                        loveLanguage = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={aboutMe={loveLanguage=$new}}}}"
        }
    }

    @Test
    fun `updating profile aboutMe children should update successfully`() {
        val new = Children.I_HAVE_AND_WANT_MORE
        testUpdateUser(
            mutationReturns = "profile { aboutMe { children } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    aboutMe = AboutMe(
                        children = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={aboutMe={children=$new}}}}"
        }
    }

    @Test
    fun `updating profile aboutMe relationship should update successfully`() {
        val new = Relationship.CONTEMPORARY
        testUpdateUser(
            mutationReturns = "profile { aboutMe { relationship } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    aboutMe = AboutMe(
                        relationship = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={aboutMe={relationship=$new}}}}"
        }
    }

    @Test
    fun `updating profile aboutMe diet should update successfully`() {
        val new = Diet.OMNIVORE
        testUpdateUser(
            mutationReturns = "profile { aboutMe { diet } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    aboutMe = AboutMe(
                        diet = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={aboutMe={diet=$new}}}}"
        }
    }

    @Test
    fun `updating profile aboutMe pets should update successfully`() {
        val new = Pet.CATS
        testUpdateUser(
            mutationReturns = "profile { aboutMe { pets } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    aboutMe = AboutMe(
                        pets = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={aboutMe={pets=$new}}}}"
        }
    }

    @Test
    fun `updating profile aboutMe zodiac should update successfully`() {
        val new = Zodiac.AQUARIUS
        testUpdateUser(
            mutationReturns = "profile { aboutMe { zodiac } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    aboutMe = AboutMe(
                        zodiac = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={aboutMe={zodiac=$new}}}}"
        }
    }

    @Test
    fun `updating profile aboutMe politics should update successfully`() {
        val new = Politics.NON_POLITICAL
        testUpdateUser(
            mutationReturns = "profile { aboutMe { politics } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    aboutMe = AboutMe(
                        politics = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={aboutMe={politics=$new}}}}"
        }
    }

    @Test
    fun `updating profile aboutMe religion should update successfully`() {
        val new = Religion.ATHEISM
        testUpdateUser(
            mutationReturns = "profile { aboutMe { religion } }",
            variables = mapOf(
                "profileUpdateInput" to ProfileUpdateInput(
                    aboutMe = AboutMe(
                        religion = new,
                    ),
                ),
            ),
        ) {
            data.toString() shouldBeEqualTo "{${OPERATION_NAME}={profile={aboutMe={religion=$new}}}}"
        }
    }

    private fun testUpdateUser(
        doBefore: suspend () -> ObjectId? = { userCollection.getUserId() },
        mutationReturns: String,
        variables: Map<String, Any>,
        assertion: suspend GraphQLResponse<*>.(HttpResponse) -> Unit,
    ) = test(
        doBefore = doBefore,
        operation = "mutation $OPERATION_NAME(\$userUpdateInput: UserUpdateInput " +
            "\$profileUpdateInput: ProfileUpdateInput \$personalDetailsUpdateInput: PersonalDetailsUpdateInput)" +
            "{ $OPERATION_NAME(userUpdateInput: \$userUpdateInput profileUpdateInput: " +
            "\$profileUpdateInput personalDetailsUpdateInput: \$personalDetailsUpdateInput) { $mutationReturns }}",
        operationName = OPERATION_NAME,
        headers = { authorizationHeader(it) },
        variables = variables,
    ) {
        assertion(this, it)
    }
}
