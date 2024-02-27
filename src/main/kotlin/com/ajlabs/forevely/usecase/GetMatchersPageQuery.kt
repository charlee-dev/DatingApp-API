package com.ajlabs.forevely.usecase

import com.ajlabs.forevely.domain.util.ErrorMessage
import com.ajlabs.forevely.domain.util.OBJECT_ID
import com.ajlabs.forevely.domain.util.getInfo
import com.ajlabs.forevely.domain.util.haversine
import com.ajlabs.forevely.domain.util.kmToRadians
import com.ajlabs.forevely.domain.util.withCurrentUser
import com.ajlabs.forevely.model.Language
import com.ajlabs.forevely.model.PageInput
import com.ajlabs.forevely.model.PagingInfo
import com.ajlabs.forevely.model.PagingInfoDesc
import com.ajlabs.forevely.model.user.AboutMe
import com.ajlabs.forevely.model.user.AboutMeDesc
import com.ajlabs.forevely.model.user.Children
import com.ajlabs.forevely.model.user.CurrentLoc
import com.ajlabs.forevely.model.user.Diet
import com.ajlabs.forevely.model.user.Drinking
import com.ajlabs.forevely.model.user.Education
import com.ajlabs.forevely.model.user.Fitness
import com.ajlabs.forevely.model.user.Gender
import com.ajlabs.forevely.model.user.Location
import com.ajlabs.forevely.model.user.LoveLanguage
import com.ajlabs.forevely.model.user.PersonalDetails
import com.ajlabs.forevely.model.user.PersonalDetailsDesc
import com.ajlabs.forevely.model.user.Personality
import com.ajlabs.forevely.model.user.Pet
import com.ajlabs.forevely.model.user.Politics
import com.ajlabs.forevely.model.user.Profile
import com.ajlabs.forevely.model.user.ProfileDesc
import com.ajlabs.forevely.model.user.Relationship
import com.ajlabs.forevely.model.user.Religion
import com.ajlabs.forevely.model.user.SimpleLoc
import com.ajlabs.forevely.model.user.Smoking
import com.ajlabs.forevely.model.user.User
import com.ajlabs.forevely.model.user.UserDesc
import com.ajlabs.forevely.model.user.Verification
import com.ajlabs.forevely.model.user.Zodiac
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import com.mongodb.client.model.Filters
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import graphql.GraphQLException
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.koin.java.KoinJavaComponent

private const val MIN_AGE = 18
private const val MAX_AGE = 100
private const val MAX_DISTANCE = 500
private const val AGE_STEP = 5
private const val DISTANCE_STEP = 10

object GetMatchersPageQuery : Query {
    private val database: MongoDatabase by KoinJavaComponent.inject(MongoDatabase::class.java)
    private val userCollection: MongoCollection<User> = database.getCollection<User>(User::class.java.simpleName)

    @Suppress("unused")
    @GraphQLDescription("Get paged matchers filtered by matcher filter")
    suspend fun getMatchersPage(
        dfe: DataFetchingEnvironment,
        @GraphQLDescription(MatcherFilterDesc.MODEL)
        matcherFilter: MatcherFilter,
        @GraphQLDescription(MatchersPageDesc.MODEL)
        pageInput: PageInput,
    ): MatchersPage = dfe.withCurrentUser { userId ->
        pageInput.validate()

        val currentUser = userCollection.find(Filters.eq(OBJECT_ID, userId)).firstOrNull()
            ?: throw GraphQLException(ErrorMessage.NOT_FOUND)

        val matchersPage = getFilteredByMatcherFilter(currentUser, matcherFilter, pageInput)

        if (matchersPage.matchers.isNotEmpty()) {
            return@withCurrentUser matchersPage
        }

        var newFilter = matcherFilter

        var hasUpdatedAgeMin = false
        var hasUpdatedAgeMax = false
        var hasUpdatedDistanceMax = false

        if (newFilter.hasAgeSafeMargin == true) {
            newFilter.ageMax?.let { ageMax ->
                if (ageMax < MAX_AGE) {
                    newFilter = newFilter.copy(ageMax = ageMax + AGE_STEP)
                    hasUpdatedAgeMax = true
                }
            }
            newFilter.ageMin?.let { ageMin ->
                val newAgeMin = ageMin - AGE_STEP
                if (MIN_AGE <= newAgeMin) {
                    newFilter = newFilter.copy(ageMin = newAgeMin)
                    hasUpdatedAgeMin = true
                }
            }
        }

        if (newFilter.hasDistanceSafeMargin == true) {
            newFilter.distanceMax?.let { distanceMax ->
                if (distanceMax < MAX_DISTANCE) {
                    newFilter = newFilter.copy(distanceMax = distanceMax + DISTANCE_STEP)
                    hasUpdatedDistanceMax = true
                }
            }
        }

        return@withCurrentUser if (hasUpdatedAgeMin || hasUpdatedAgeMax || hasUpdatedDistanceMax) {
            val newPageInput = pageInput.copy(page = 1)
            getMatchersPage(dfe, newFilter, newPageInput)
        } else {
            matchersPage
        }
    }

    private suspend fun getFilteredByMatcherFilter(
        currentUser: User,
        matcherFilter: MatcherFilter,
        pageInput: PageInput,
    ): MatchersPage {
        val bson = mutableListOf<Bson>(Filters.ne(OBJECT_ID, currentUser.id)).apply {
            with(matcherFilter) {
                genders?.let {
                    add(
                        Filters.`in`(
                            "${User::details.name}.${PersonalDetails::gender.name}",
                            it,
                        ),
                    )
                }
                ageMin?.let {
                    if (matcherFilter.hasAgeMinLimit == true) {
                        add(Filters.gte("${User::details.name}.${PersonalDetails::age.name}", it))
                    }
                }
                ageMax?.let {
                    if (matcherFilter.hasAgeMaxLimit == true) {
                        add(Filters.lte("${User::details.name}.${PersonalDetails::age.name}", it))
                    }
                }
                distanceMax?.let { maxDistance ->
                    if (matcherFilter.hasDistanceLimit == true) {
                        val geo = currentUser.details.currentLoc?.geo
                            ?: throw GraphQLException("User goe point " + ErrorMessage.NOT_FOUND)

                        add(
                            Filters.geoWithinCenterSphere(
                                "${User::details.name}.${PersonalDetails::currentLoc.name}.${Location::geo.name}",
                                geo.coordinates.values[0],
                                geo.coordinates.values[1],
                                kmToRadians(maxDistance),
                            ),
                        )
                    }
                }
                languages?.let {
                    if (it.isNotEmpty()) {
                        add(
                            Filters.`in`(
                                "${User::profile.name}.${Profile::aboutMe.name}.${AboutMe::languages.name}",
                                it,
                            ),
                        )
                    }
                }
                verifiedProfilesOnly?.let {
                    // TODO: Handle other types of verification if needed
                    add(Filters.eq("${User::verification.name}.${Verification::emailVerified.name}", it))
                }
                heightMin?.let {
                    if (matcherFilter.hasHeightMinLimit == true) {
                        add(Filters.gte("${User::profile.name}.${Profile::aboutMe.name}.${AboutMe::height.name}", it))
                    }
                }
                heightMax?.let {
                    if (matcherFilter.hasHeightMaxLimit == true) {
                        add(Filters.lte("${User::profile.name}.${Profile::aboutMe.name}.${AboutMe::height.name}", it))
                    }
                }
                fitnesses?.let {
                    add(
                        Filters.`in`(
                            "${User::profile.name}.${Profile::aboutMe.name}.${AboutMe::fitness.name}",
                            it,
                        ),
                    )
                }
                educations?.let {
                    add(
                        Filters.`in`(
                            "${User::profile.name}.${Profile::aboutMe.name}.${AboutMe::education.name}",
                            it,
                        ),
                    )
                }
                drinkings?.let {
                    add(
                        Filters.`in`(
                            "${User::profile.name}.${Profile::aboutMe.name}.${AboutMe::drinking.name}",
                            it,
                        ),
                    )
                }
                smokings?.let {
                    add(
                        Filters.`in`(
                            "${User::profile.name}.${Profile::aboutMe.name}.${AboutMe::smoking.name}",
                            it,
                        ),
                    )
                }
                children?.let {
                    add(
                        Filters.`in`(
                            "${User::profile.name}.${Profile::aboutMe.name}.${AboutMe::children.name}",
                            it,
                        ),
                    )
                }
                zodiacs?.let {
                    add(
                        Filters.`in`(
                            "${User::profile.name}.${Profile::aboutMe.name}.${AboutMe::zodiac.name}",
                            it,
                        ),
                    )
                }
                politics?.let {
                    add(
                        Filters.`in`(
                            "${User::profile.name}.${Profile::aboutMe.name}.${AboutMe::politics.name}",
                            it,
                        ),
                    )
                }
                religions?.let {
                    add(
                        Filters.`in`(
                            "${User::profile.name}.${Profile::aboutMe.name}.${AboutMe::religion.name}",
                            it,
                        ),
                    )
                }
                diets?.let {
                    add(
                        Filters.`in`(
                            "${User::profile.name}.${Profile::aboutMe.name}.${AboutMe::diet.name}",
                            it,
                        ),
                    )
                }
                loveLanguages?.let {
                    add(
                        Filters.`in`(
                            "${User::profile.name}.${Profile::aboutMe.name}.${AboutMe::loveLanguage.name}",
                            it,
                        ),
                    )
                }
                personalities?.let {
                    add(
                        Filters.`in`(
                            "${User::profile.name}.${Profile::aboutMe.name}.${AboutMe::personality.name}",
                            it,
                        ),
                    )
                }
                petPrefs?.let {
                    add(
                        Filters.`in`(
                            "${User::profile.name}.${Profile::aboutMe.name}.${AboutMe::pets.name}",
                            it,
                        ),
                    )
                }
            }
        }

        val filters = Filters.and(bson)
        val skips = (pageInput.page - 1) * pageInput.size
        val documents = userCollection.find<UserWithMatcherFields>(filters)
            .skip(skips)
            .limit(pageInput.size)
            .partial(true)
            .toList()
        val totalDocuments = userCollection.countDocuments(filters)
        val pagingInfo = getInfo(totalDocuments.toInt(), pageInput)

        val matchers = documents.map {
            // Calculate distance between current user and matcher
            val currentUserLat = currentUser.details.currentLoc?.geo?.coordinates?.values?.get(0)
            val currentUserLng = currentUser.details.currentLoc?.geo?.coordinates?.values?.get(1)

            val matcherLat = it.details.currentLoc?.geo?.coordinates?.values?.get(0)
            val matcherLng = it.details.currentLoc?.geo?.coordinates?.values?.get(1)

            val distance = if (
                currentUserLat != null &&
                currentUserLng != null &&
                matcherLat != null &&
                matcherLng != null
            ) {
                haversine(currentUserLat, currentUserLng, matcherLat, matcherLng).toInt()
            } else {
                null
            }

            it.toMatcher(distance)
        }
        return MatchersPage(matchers, pagingInfo)
    }
}

@GraphQLDescription(MatchersPageDesc.MODEL)
data class MatchersPage(
    @GraphQLDescription(MatchersPageDesc.MATCHER_USERS)
    val matchers: List<Matcher>,
    @GraphQLDescription(MatchersPageDesc.INFO)
    val info: PagingInfo,
)

data class Matcher(
    @BsonId
    @GraphQLDescription(UserDesc.ID)
    val id: String,
    @GraphQLDescription(PersonalDetailsDesc.NAME)
    val name: String?,
    @GraphQLDescription(PersonalDetailsDesc.AGE)
    val age: Int?,
    @GraphQLDescription(MatcherDesc.VERIFIED)
    val verified: Boolean,
    @GraphQLDescription(AboutMeDesc.HEIGHT)
    val height: Int?,
    @GraphQLDescription(AboutMeDesc.FITNESS)
    val fitness: Fitness?,
    @GraphQLDescription(AboutMeDesc.EDUCATION)
    val education: Education?,
    @GraphQLDescription(AboutMeDesc.DRINKING)
    val drinking: Drinking?,
    @GraphQLDescription(AboutMeDesc.SMOKING)
    val smoking: Smoking?,
    @GraphQLDescription(PersonalDetailsDesc.GENDER)
    val gender: Gender?,
    @GraphQLDescription(AboutMeDesc.RELATIONSHIP)
    val relationship: Relationship?,
    @GraphQLDescription(AboutMeDesc.CHILDREN)
    val children: Children?,
    @GraphQLDescription(AboutMeDesc.ZODIAC)
    val zodiac: Zodiac?,
    @GraphQLDescription(AboutMeDesc.DIET)
    val diet: Diet?,
    @GraphQLDescription(AboutMeDesc.LOVE_LANGUAGE)
    val loveLanguage: LoveLanguage?,
    @GraphQLDescription(AboutMeDesc.PERSONALITY)
    val personality: Personality?,
    @GraphQLDescription(AboutMeDesc.PETS)
    val pets: Pet?,
    @GraphQLDescription(AboutMeDesc.RELIGION)
    val religion: Religion?,
    @GraphQLDescription(AboutMeDesc.POLITICS)
    val politics: Politics?,
    @GraphQLDescription(AboutMeDesc.LANGUAGES)
    val languages: List<Language>,
    @GraphQLDescription(ProfileDesc.PICTURES)
    val pictures: List<String>,
    @GraphQLDescription(PersonalDetailsDesc.CURRENT_LOC)
    val currentLoc: CurrentLoc?,
    @GraphQLDescription(PersonalDetailsDesc.LIVE_LOC)
    val liveLoc: SimpleLoc?,
    @GraphQLDescription(PersonalDetailsDesc.BIRTH_LOC)
    val birthLoc: SimpleLoc?,
)

object MatcherDesc {
    const val VERIFIED = "Whether the user is verified"
}

data class UserWithMatcherFields(
    @BsonId
    val id: ObjectId,
    val profile: Profile,
    val details: PersonalDetailsWithMatcherFields,
    val verification: Verification,
)

data class PersonalDetailsWithMatcherFields(
    val name: String? = null,
    val age: Int? = null,
    val job: String? = null,
    val education: Education? = null,
    val gender: Gender? = null,
    val currentLoc: Location? = null,
    val liveLoc: SimpleLoc? = null,
    val birthLoc: SimpleLoc? = null,
)

fun UserWithMatcherFields.toMatcher(distance: Int?): Matcher {
    return Matcher(
        id = id.toString(),
        name = details.name,
        age = details.age,
        verified = verification.emailVerified, // FIXME: For now only checking email verification
        height = profile.aboutMe.height,
        fitness = profile.aboutMe.fitness,
        education = profile.aboutMe.education,
        drinking = profile.aboutMe.drinking,
        smoking = profile.aboutMe.smoking,
        gender = details.gender,
        relationship = profile.aboutMe.relationship,
        children = profile.aboutMe.children,
        zodiac = profile.aboutMe.zodiac,
        diet = profile.aboutMe.diet,
        loveLanguage = profile.aboutMe.loveLanguage,
        languages = profile.aboutMe.languages,
        personality = profile.aboutMe.personality,
        religion = profile.aboutMe.religion,
        politics = profile.aboutMe.politics,
        pets = profile.aboutMe.pets,
        pictures = profile.pictures,
        currentLoc = CurrentLoc(
            longitude = details.currentLoc?.geo?.coordinates?.values?.get(0),
            latitude = details.currentLoc?.geo?.coordinates?.values?.get(1),
            city = details.currentLoc?.city,
            state = details.currentLoc?.state,
            country = details.currentLoc?.country,
            distance = distance,
        ),
        liveLoc = details.liveLoc,
        birthLoc = details.birthLoc,
    )
}

private object MatchersPageDesc {
    const val MODEL = "Represents a paginated list of matcher user objects"
    const val MATCHER_USERS = "The list of matcher users on this page"
    const val INFO = PagingInfoDesc.MODEL
}

@GraphQLDescription(MatcherFilterDesc.MODEL)
data class MatcherFilter(
    @GraphQLDescription(MatcherFilterDesc.HAS_AGE_MIN_LIMIT)
    val hasAgeMinLimit: Boolean? = null,
    @GraphQLDescription(MatcherFilterDesc.HAS_AGE_MAX_LIMIT)
    val hasAgeMaxLimit: Boolean? = null,
    @GraphQLDescription(MatcherFilterDesc.AGE_MIN)
    val ageMin: Int? = null,
    @GraphQLDescription(MatcherFilterDesc.AGE_MAX)
    val ageMax: Int? = null,
    @GraphQLDescription(MatcherFilterDesc.HAS_AGE_SAFE_MARGIN)
    val hasAgeSafeMargin: Boolean? = null,
    @GraphQLDescription(MatcherFilterDesc.HAS_DISTANCE_LIMIT)
    val hasDistanceLimit: Boolean? = null,
    @GraphQLDescription(MatcherFilterDesc.MAX_DISTANCE_AWAY)
    val distanceMax: Int? = null,
    @GraphQLDescription(MatcherFilterDesc.HAS_DISTANCE_SAFE_MARGIN)
    val hasDistanceSafeMargin: Boolean? = null,
    @GraphQLDescription(MatcherFilterDesc.GENDERS)
    val genders: List<Gender>? = null,
    @GraphQLDescription(AboutMeDesc.LANGUAGES)
    val languages: List<Language>? = null,
    @GraphQLDescription(MatcherFilterDesc.VERIFIED_ONLY)
    val verifiedProfilesOnly: Boolean? = null,
    @GraphQLDescription(MatcherFilterDesc.HAS_HEIGHT_MIN_LIMIT)
    val hasHeightMinLimit: Boolean? = null,
    @GraphQLDescription(MatcherFilterDesc.HAS_HEIGHT_MAX_LIMIT)
    val hasHeightMaxLimit: Boolean? = null,
    @GraphQLDescription(MatcherFilterDesc.HEIGHT_MIN)
    val heightMin: Int? = null,
    @GraphQLDescription(MatcherFilterDesc.HEIGHT_MAX)
    val heightMax: Int? = null,
    @GraphQLDescription(MatcherFilterDesc.HEIGHT_SAFE_MARGIN)
    val heightSafeMargin: Boolean? = null,
    @GraphQLDescription(AboutMeDesc.FITNESS)
    val fitnesses: List<Fitness>? = null,
    @GraphQLDescription(AboutMeDesc.EDUCATION)
    val educations: List<Education>? = null,
    @GraphQLDescription(AboutMeDesc.DRINKING)
    val drinkings: List<Drinking>? = null,
    @GraphQLDescription(AboutMeDesc.SMOKING)
    val smokings: List<Smoking>? = null,
    @GraphQLDescription(AboutMeDesc.CHILDREN)
    val children: List<Children>? = null,
    @GraphQLDescription(AboutMeDesc.ZODIAC)
    val zodiacs: List<Zodiac>? = null,
    @GraphQLDescription(AboutMeDesc.RELATIONSHIP)
    val relationship: List<Relationship>? = null,
    @GraphQLDescription(AboutMeDesc.POLITICS)
    val politics: List<Politics>? = null,
    @GraphQLDescription(AboutMeDesc.RELIGION)
    val religions: List<Religion>? = null,
    @GraphQLDescription(AboutMeDesc.DIET)
    val diets: List<Diet>? = null,
    @GraphQLDescription(AboutMeDesc.LOVE_LANGUAGE)
    val loveLanguages: List<LoveLanguage>? = null,
    @GraphQLDescription(AboutMeDesc.PERSONALITY)
    val personalities: List<Personality>? = null,
    @GraphQLDescription(AboutMeDesc.PETS)
    val petPrefs: List<Pet>? = null,
)

object MatcherFilterDesc {
    const val MODEL = "Represents a matcher filter object"
    const val HAS_AGE_MIN_LIMIT = "Has age min limit"
    const val HAS_AGE_MAX_LIMIT = "Has age max limit"
    const val AGE_MIN = "Minimum age chosen range. Whole number"
    const val AGE_MAX = "Maximum age chosen range. Whole number"
    const val HAS_AGE_SAFE_MARGIN = "Has age safe margin"
    const val HAS_DISTANCE_LIMIT = "Has distance limit"
    const val MAX_DISTANCE_AWAY = "Maximum distance away in km. Example '11' km away"
    const val HAS_DISTANCE_SAFE_MARGIN = "Has distance safe margin"
    const val GENDERS = "Chosen genders"
    const val VERIFIED_ONLY = "Verified profiles only"
    const val HAS_HEIGHT_MIN_LIMIT = "Has height min limit"
    const val HAS_HEIGHT_MAX_LIMIT = "Has height max limit"
    const val HEIGHT_MIN = "Minimum height chosen range"
    const val HEIGHT_MAX = "Maximum height chosen range"
    const val HEIGHT_SAFE_MARGIN = "Height safe margin"
}
