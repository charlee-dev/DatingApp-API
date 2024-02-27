package com.ajlabs.forevely.feature.matcher

import com.ajlabs.forevely.domain.util.haversine
import com.ajlabs.forevely.helpers.MongoTests
import com.ajlabs.forevely.mock.mockUsers
import com.ajlabs.forevely.mock.user1
import com.ajlabs.forevely.mock.user2
import com.ajlabs.forevely.mock.user3
import com.ajlabs.forevely.mock.user4
import com.ajlabs.forevely.model.Language
import com.ajlabs.forevely.model.PageInput
import com.ajlabs.forevely.model.user.Children
import com.ajlabs.forevely.model.user.Diet
import com.ajlabs.forevely.model.user.Drinking
import com.ajlabs.forevely.model.user.Education
import com.ajlabs.forevely.model.user.Fitness
import com.ajlabs.forevely.model.user.Gender
import com.ajlabs.forevely.model.user.LoveLanguage
import com.ajlabs.forevely.model.user.Personality
import com.ajlabs.forevely.model.user.Pet
import com.ajlabs.forevely.model.user.Politics
import com.ajlabs.forevely.model.user.Religion
import com.ajlabs.forevely.model.user.Smoking
import com.ajlabs.forevely.model.user.User
import com.ajlabs.forevely.model.user.Zodiac
import com.ajlabs.forevely.plugins.configureKoin
import com.ajlabs.forevely.usecase.MatcherFilter
import com.expediagroup.graphql.server.types.GraphQLResponse
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be`
import org.amshove.kluent.shouldBeEqualTo
import org.bson.types.ObjectId
import org.junit.jupiter.api.Test
import org.koin.core.context.stopKoin

private const val OPERATION_NAME = "getMatchersPage"
private const val MATCHERS = "matchers"

class GetMatchersPageTests : MongoTests() {
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

    @Test
    fun `no filters returns all users except current user`() = testMatcherFilters(
        matcherReturn = "age",
        matcherFilter = MatcherFilter(),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{age=${user2.details.age}}, " +
            "{age=${user3.details.age}}, " +
            "{age=${user4.details.age}}]}}"
    }

    @Test
    fun `if has age min limit then filter by min age should return users older then 40`() = testMatcherFilters(
        matcherReturn = "age",
        matcherFilter = MatcherFilter(
            hasAgeMinLimit = true,
            ageMin = 40,
        ),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{age=${user3.details.age}}, " +
            "{age=${user4.details.age}}]}}"
    }

    @Test
    fun `if has no age min limit then filter by min age should return users not filtered`() = testMatcherFilters(
        matcherReturn = "age",
        matcherFilter = MatcherFilter(ageMin = 40),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{age=${user2.details.age}}, " +
            "{age=${user3.details.age}}, " +
            "{age=${user4.details.age}}]}}"
    }

    @Test
    fun `if has age max limit then filter by max age should return users younger then 40`() = testMatcherFilters(
        matcherReturn = "age",
        matcherFilter = MatcherFilter(
            hasAgeMaxLimit = true,
            ageMax = 40,
        ),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[{age=${user2.details.age}}]}}"
    }

    @Test
    fun `if has no age max limit then filter by max age should return not filtered`() = testMatcherFilters(
        matcherReturn = "age",
        matcherFilter = MatcherFilter(ageMax = 40),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{age=${user2.details.age}}, " +
            "{age=${user3.details.age}}, " +
            "{age=${user4.details.age}}]}}"
    }

    @Test
    fun `filter by maxDistanceAway should return 1 user closer then 11km`() = testMatcherFilters(
        matcherReturn = "age",
        matcherFilter = MatcherFilter(
            hasDistanceLimit = true,
            distanceMax = 11,
        ),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[{age=${user2.details.age}}]}}"
    }

    @Test
    fun `filter by one knownLanguage should return users knowing only chosen languages`() = testMatcherFilters(
        matcherReturn = "languages",
        matcherFilter = MatcherFilter(languages = listOf(Language.Polish)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{languages=${user2.profile.aboutMe.languages}}, " +
            "{languages=${user4.profile.aboutMe.languages}}]}}"
    }

    @Test
    fun `filter by multiple knownLanguagesPrefs should return users knowing only chosen languages`() =
        testMatcherFilters(
            matcherReturn = "languages",
            matcherFilter = MatcherFilter(languages = listOf(Language.Polish, Language.Spanish)),
        ) {
            data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
                "{languages=${user2.profile.aboutMe.languages}}, " +
                "{languages=${user3.profile.aboutMe.languages}}, " +
                "{languages=${user4.profile.aboutMe.languages}}]}}"
        }

    @Test
    fun `filter by verifiedProfilesOnly should return verified users`() = testMatcherFilters(
        matcherReturn = "verified",
        matcherFilter = MatcherFilter(verifiedProfilesOnly = true),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{verified=${user3.verification.emailVerified}}, " +
            "{verified=${user4.verification.emailVerified}}]}}"
    }

    @Test
    fun `filter by heightChosenRangeMin should return users higher then chosen value`() = testMatcherFilters(
        matcherReturn = "height",
        matcherFilter = MatcherFilter(
            hasHeightMinLimit = true,
            heightMin = 140,
        ),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{height=${user3.profile.aboutMe.height}}, " +
            "{height=${user4.profile.aboutMe.height}}]}}"
    }

    @Test
    fun `if has height max limit then filter by heightChosenRangeMax should return users higher then chosen value`() =
        testMatcherFilters(
            matcherReturn = "height",
            matcherFilter = MatcherFilter(
                hasHeightMaxLimit = true,
                heightMax = 150,
            ),
        ) {
            data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
                "{height=${user2.profile.aboutMe.height}}, " +
                "{height=${user3.profile.aboutMe.height}}]}}"
        }

    @Test
    fun `if has no height max limit then filter by heightChosenRangeMax should return users not filtered`() =
        testMatcherFilters(
            matcherReturn = "height",
            matcherFilter = MatcherFilter(
                heightMax = 150,
            ),
        ) {
            data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
                "{height=${user2.profile.aboutMe.height}}, " +
                "{height=${user3.profile.aboutMe.height}}, " +
                "{height=${user4.profile.aboutMe.height}}]}}"
        }

    @Test
    fun `if has no distance limit then filter by heightChosenRangeMax should not filter`() =
        testMatcherFilters(
            matcherReturn = "height",
            matcherFilter = MatcherFilter(
                hasDistanceLimit = true,
                heightMax = 150,
            ),
        ) {
            data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
                "{height=${user2.profile.aboutMe.height}}, " +
                "{height=${user3.profile.aboutMe.height}}, " +
                "{height=${user4.profile.aboutMe.height}}]}}"
        }

    @Test
    fun `if has height min max limit filter by heightChosenRangeMin and heightChosenRangeMax should return users in chosen range`() =
        testMatcherFilters(
            matcherReturn = "height",
            matcherFilter = MatcherFilter(
                hasHeightMinLimit = true,
                hasHeightMaxLimit = true,
                heightMin = 130,
                heightMax = 150,
            ),
        ) {
            data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[{height=${user3.profile.aboutMe.height}}]}}"
        }

    @Test
    fun `if has height min max limit filter by heightChosenRangeMin and heightChosenRangeMax should return users not filtered`() =
        testMatcherFilters(
            matcherReturn = "height",
            matcherFilter = MatcherFilter(
                heightMin = 130,
                heightMax = 150,
            ),
        ) {
            data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
                "{height=${user2.profile.aboutMe.height}}, " +
                "{height=${user3.profile.aboutMe.height}}, " +
                "{height=${user4.profile.aboutMe.height}}]}}"
        }

    @Test
    fun `filter by one fitness should return matchers`() = testMatcherFilters(
        matcherReturn = "fitness",
        matcherFilter = MatcherFilter(fitnesses = listOf(Fitness.OFTEN)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{fitness=${user3.profile.aboutMe.fitness}}]}}"
    }

    @Test
    fun `filter by multiple fitness should return matchers`() = testMatcherFilters(
        matcherReturn = "fitness",
        matcherFilter = MatcherFilter(fitnesses = listOf(Fitness.OFTEN, Fitness.SOMETIMES)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{fitness=${user2.profile.aboutMe.fitness}}, " +
            "{fitness=${user3.profile.aboutMe.fitness}}]}}"
    }

    @Test
    fun `filter by one education should return matchers`() = testMatcherFilters(
        matcherReturn = "education",
        matcherFilter = MatcherFilter(educations = listOf(Education.PHD)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{education=${user2.profile.aboutMe.education}}]}}"
    }

    @Test
    fun `filter by multiple education should return matchers`() = testMatcherFilters(
        matcherReturn = "education",
        matcherFilter = MatcherFilter(educations = listOf(Education.PHD, Education.MASTERS)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{education=${user2.profile.aboutMe.education}}, " +
            "{education=${user4.profile.aboutMe.education}}]}}"
    }

    @Test
    fun `filter by one drinking should return matchers`() = testMatcherFilters(
        matcherReturn = "drinking",
        matcherFilter = MatcherFilter(drinkings = listOf(Drinking.REGULAR_DRINKER)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{drinking=${user2.profile.aboutMe.drinking}}]}}"
    }

    @Test
    fun `filter by multiple drinking should return matchers`() = testMatcherFilters(
        matcherReturn = "drinking",
        matcherFilter = MatcherFilter(drinkings = listOf(Drinking.SOBER, Drinking.HEAVY_DRINKER)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{drinking=${user3.profile.aboutMe.drinking}}, " +
            "{drinking=${user4.profile.aboutMe.drinking}}]}}"
    }

    @Test
    fun `filter by one smoking should return matchers`() = testMatcherFilters(
        matcherReturn = "smoking",
        matcherFilter = MatcherFilter(smokings = listOf(Smoking.SMOKER_WHEN_DRINKING)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{smoking=${user2.profile.aboutMe.smoking}}]}}"
    }

    @Test
    fun `filter by multiple smoking should return matchers`() = testMatcherFilters(
        matcherReturn = "smoking",
        matcherFilter = MatcherFilter(
            smokings = listOf(
                Smoking.SMOKER_WHEN_DRINKING,
                Smoking.TRYING_TO_QUIT,
            ),
        ),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{smoking=${user2.profile.aboutMe.smoking}}, " +
            "{smoking=${user4.profile.aboutMe.smoking}}]}}"
    }

    @Test
    fun `filter by one child should return matchers`() = testMatcherFilters(
        matcherReturn = "children",
        matcherFilter = MatcherFilter(children = listOf(Children.I_HAVE_AND_WANT_MORE)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{children=${user2.profile.aboutMe.children}}, " +
            "{children=${user3.profile.aboutMe.children}}]}}"
    }

    @Test
    fun `filter by multiple children should return matchers`() = testMatcherFilters(
        matcherReturn = "children",
        matcherFilter = MatcherFilter(
            children = listOf(
                Children.I_HAVE_AND_DONT_WANT_MORE,
                Children.I_HAVE_AND_WANT_MORE,
            ),
        ),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{children=${user2.profile.aboutMe.children}}, " +
            "{children=${user3.profile.aboutMe.children}}, " +
            "{children=${user4.profile.aboutMe.children}}]}}"
    }

    @Test
    fun `filter by one zodiac should return matchers`() = testMatcherFilters(
        matcherReturn = "zodiac",
        matcherFilter = MatcherFilter(zodiacs = listOf(Zodiac.ARIES)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{zodiac=${user2.profile.aboutMe.zodiac}}]}}"
    }

    @Test
    fun `filter by multiple zodiac should return matchers`() = testMatcherFilters(
        matcherReturn = "zodiac",
        matcherFilter = MatcherFilter(zodiacs = listOf(Zodiac.ARIES, Zodiac.LIBRA)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{zodiac=${user2.profile.aboutMe.zodiac}}, " +
            "{zodiac=${user4.profile.aboutMe.zodiac}}]}}"
    }

    @Test
    fun `filter by one politics should return matchers`() = testMatcherFilters(
        matcherReturn = "politics",
        matcherFilter = MatcherFilter(politics = listOf(Politics.RIGHT_WING)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{politics=${user2.profile.aboutMe.politics}}]}}"
    }

    @Test
    fun `filter by multiple politics should return matchers`() = testMatcherFilters(
        matcherReturn = "politics",
        matcherFilter = MatcherFilter(politics = listOf(Politics.NON_POLITICAL, Politics.LEFT_WING)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{politics=${user3.profile.aboutMe.politics}}, " +
            "{politics=${user4.profile.aboutMe.politics}}]}}"
    }

    @Test
    fun `filter by one religion should return matchers`() = testMatcherFilters(
        matcherReturn = "religion",
        matcherFilter = MatcherFilter(religions = listOf(Religion.CHRISTIANITY)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{religion=${user2.profile.aboutMe.religion}}]}}"
    }

    @Test
    fun `filter by multiple religions should return matchers`() = testMatcherFilters(
        matcherReturn = "religion",
        matcherFilter = MatcherFilter(religions = listOf(Religion.CHRISTIANITY, Religion.ISLAM)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{religion=${user2.profile.aboutMe.religion}}, " +
            "{religion=${user3.profile.aboutMe.religion}}]}}"
    }

    @Test
    fun `filter by one diet should return matchers`() = testMatcherFilters(
        matcherReturn = "diet",
        matcherFilter = MatcherFilter(diets = listOf(Diet.HALAL)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{diet=${user2.profile.aboutMe.diet}}]}}"
    }

    @Test
    fun `filter by multiple diets should return matchers`() = testMatcherFilters(
        matcherReturn = "diet",
        matcherFilter = MatcherFilter(diets = listOf(Diet.KOSHER, Diet.VEGETARIAN)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{diet=${user3.profile.aboutMe.diet}}, " +
            "{diet=${user4.profile.aboutMe.diet}}]}}"
    }

    @Test
    fun `filter by one loveLanguage should return matchers`() = testMatcherFilters(
        matcherReturn = "loveLanguage",
        matcherFilter = MatcherFilter(loveLanguages = listOf(LoveLanguage.RECEIVING_GIFTS)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{loveLanguage=${user3.profile.aboutMe.loveLanguage}}]}}"
    }

    @Test
    fun `filter by multiple loveLanguages should return matchers`() = testMatcherFilters(
        matcherReturn = "loveLanguage",
        matcherFilter = MatcherFilter(
            loveLanguages = listOf(
                LoveLanguage.ACTS_OF_SERVICE,
                LoveLanguage.RECEIVING_GIFTS,
            ),
        ),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{loveLanguage=${user3.profile.aboutMe.loveLanguage}}, " +
            "{loveLanguage=${user4.profile.aboutMe.loveLanguage}}]}}"
    }

    @Test
    fun `filter by one personality should return matchers`() = testMatcherFilters(
        matcherReturn = "personality",
        matcherFilter = MatcherFilter(personalities = listOf(Personality.INFJ)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{personality=${user2.profile.aboutMe.personality}}]}}"
    }

    @Test
    fun `filter by multiple personality should return matchers`() = testMatcherFilters(
        matcherReturn = "personality",
        matcherFilter = MatcherFilter(personalities = listOf(Personality.ESFJ, Personality.INTJ)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{personality=${user3.profile.aboutMe.personality}}, " +
            "{personality=${user4.profile.aboutMe.personality}}]}}"
    }

    @Test
    fun `filter by one pet should return matchers`() = testMatcherFilters(
        matcherReturn = "pets",
        matcherFilter = MatcherFilter(petPrefs = listOf(Pet.CATS)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{pets=${user3.profile.aboutMe.pets}}]}}"
    }

    @Test
    fun `filter by multiple pets should return matchers`() = testMatcherFilters(
        matcherReturn = "pets",
        matcherFilter = MatcherFilter(petPrefs = listOf(Pet.BIRDS, Pet.CATS)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{pets=${user3.profile.aboutMe.pets}}, " +
            "{pets=${user4.profile.aboutMe.pets}}]}}"
    }

    @Test
    fun `filter by one gender should return matchers`() = testMatcherFilters(
        matcherReturn = "gender",
        matcherFilter = MatcherFilter(genders = listOf(Gender.FEMALE)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{gender=${user2.details.gender}}, " +
            "{gender=${user3.details.gender}}]}}"
    }

    @Test
    fun `filter by multiple gender should return matchers`() = testMatcherFilters(
        matcherReturn = "gender",
        matcherFilter = MatcherFilter(genders = listOf(Gender.FEMALE, Gender.NON_BINARY)),
    ) {
        data?.toString() shouldBeEqualTo "{$OPERATION_NAME={$MATCHERS=[" +
            "{gender=${user2.details.gender}}, " +
            "{gender=${user3.details.gender}}, " +
            "{gender=${user4.details.gender}}]}}"
    }

    @Test
    fun `when query runs out of matches should return extra with bigger distance than max distance requested originally matchers`() =
        testMatcherFilters(
            doBefore = { userCollection.getUserId() },
            infoReturn = "info { pages count prev next }",
            matcherReturn = "currentLoc { latitude longitude }",
            matcherFilter = MatcherFilter(
                hasDistanceLimit = true,
                hasDistanceSafeMargin = true,
                distanceMax = 5,
            ),
            page = 0,
            size = 1,
        ) {
            val user2Lat = user2.details.currentLoc?.geo?.coordinates?.values?.get(1) ?: 0.0
            val user2Lon = user2.details.currentLoc?.geo?.coordinates?.values?.get(0) ?: 0.0
            data?.toString() shouldBeEqualTo "{$OPERATION_NAME={" +
                "info={pages=1, count=1, prev=0, next=2}, " +
                "$MATCHERS=[{currentLoc={latitude=$user2Lat, longitude=$user2Lon}}]}}"
        }

    @Test
    fun `when query runs out of matches should return extra with bigger distance than max distance requested originally`() {
        val result1 = haversine(
            user1.details.currentLoc?.geo?.coordinates?.values?.get(0) ?: 0.0,
            user1.details.currentLoc?.geo?.coordinates?.values?.get(1) ?: 0.0,
            user2.details.currentLoc?.geo?.coordinates?.values?.get(0) ?: 0.0,
            user2.details.currentLoc?.geo?.coordinates?.values?.get(1) ?: 0.0,
        )
        println("user1 to user 2: $result1")
        println("user2 lat: ${user2.details.currentLoc?.geo?.coordinates?.values?.get(0)}")

        val result2 = haversine(
            user1.details.currentLoc?.geo?.coordinates?.values?.get(0) ?: 0.0,
            user1.details.currentLoc?.geo?.coordinates?.values?.get(1) ?: 0.0,
            user3.details.currentLoc?.geo?.coordinates?.values?.get(0) ?: 0.0,
            user3.details.currentLoc?.geo?.coordinates?.values?.get(1) ?: 0.0,
        )
        println("user1 to user 3: $result2")

        val result3 = haversine(
            user1.details.currentLoc?.geo?.coordinates?.values?.get(0) ?: 0.0,
            user1.details.currentLoc?.geo?.coordinates?.values?.get(1) ?: 0.0,
            user4.details.currentLoc?.geo?.coordinates?.values?.get(0) ?: 0.0,
            user4.details.currentLoc?.geo?.coordinates?.values?.get(1) ?: 0.0,
        )
        println("user1 to user 4: $result3")
    }

    private fun testMatcherFilters(
        infoReturn: String = "", // or "info { pages count prev next }"
        matcherReturn: String = "age",
        page: Int = 0,
        size: Int = 10,
        matcherFilter: MatcherFilter,
        doBefore: suspend () -> ObjectId? = { userCollection.getUserId() },
        assertion: suspend GraphQLResponse<*>.(HttpResponse) -> Unit,
    ) = test(
        doBefore = doBefore,
        operation = "query $OPERATION_NAME(\$matcherFilter: MatcherFilterInput!, \$pageInput: PageInput!)" +
            "{ $OPERATION_NAME(matcherFilter: \$matcherFilter, pageInput: \$pageInput) " +
            "{ $infoReturn $MATCHERS { $matcherReturn }}}",
        operationName = OPERATION_NAME,
        headers = { authorizationHeader(it) },
        variables = mapOf(
            "matcherFilter" to matcherFilter,
            "pageInput" to PageInput(page, size),
        ),
    ) {
        errors `should be` null
        assertion(this, it)
    }
}
