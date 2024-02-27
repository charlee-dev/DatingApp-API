package com.ajlabs.forevely.usecase

import com.ajlabs.forevely.domain.InputValidator
import com.ajlabs.forevely.domain.JwtService
import com.ajlabs.forevely.domain.util.ErrorMessage
import com.ajlabs.forevely.domain.util.encrypt
import com.ajlabs.forevely.domain.util.generateSalt
import com.ajlabs.forevely.domain.util.kmToLatitudeDegrees
import com.ajlabs.forevely.model.Language
import com.ajlabs.forevely.model.auth.AuthInput
import com.ajlabs.forevely.model.auth.AuthInputDesc
import com.ajlabs.forevely.model.auth.AuthResponse
import com.ajlabs.forevely.model.user.AboutMe
import com.ajlabs.forevely.model.user.Children
import com.ajlabs.forevely.model.user.Diet
import com.ajlabs.forevely.model.user.Drinking
import com.ajlabs.forevely.model.user.Education
import com.ajlabs.forevely.model.user.Fitness
import com.ajlabs.forevely.model.user.Gender
import com.ajlabs.forevely.model.user.Interests
import com.ajlabs.forevely.model.user.Location
import com.ajlabs.forevely.model.user.LoveLanguage
import com.ajlabs.forevely.model.user.OpenQuestion
import com.ajlabs.forevely.model.user.PersonalDetails
import com.ajlabs.forevely.model.user.Personality
import com.ajlabs.forevely.model.user.Pet
import com.ajlabs.forevely.model.user.Politics
import com.ajlabs.forevely.model.user.Profile
import com.ajlabs.forevely.model.user.Relationship
import com.ajlabs.forevely.model.user.Religion
import com.ajlabs.forevely.model.user.SimpleLoc
import com.ajlabs.forevely.model.user.Smoking
import com.ajlabs.forevely.model.user.Sports
import com.ajlabs.forevely.model.user.Swipes
import com.ajlabs.forevely.model.user.User
import com.ajlabs.forevely.model.user.Verification
import com.ajlabs.forevely.model.user.Zodiac
import com.ajlabs.forevely.model.user.checkProfileCompletion
import com.ajlabs.forevely.model.user.toMinimal
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Indexes
import com.mongodb.client.model.geojson.Point
import com.mongodb.client.model.geojson.Position
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import graphql.GraphQLException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.bson.types.ObjectId
import org.koin.java.KoinJavaComponent

object RegisterMutation : Mutation {
    private val jwtService: JwtService by KoinJavaComponent.inject(JwtService::class.java)
    private val inputValidator: InputValidator by KoinJavaComponent.inject(InputValidator::class.java)
    private val database: MongoDatabase by KoinJavaComponent.inject(MongoDatabase::class.java)
    private val userCollection: MongoCollection<User> = database.getCollection<User>(User::class.java.simpleName)

    @GraphQLDescription("Signs up user with email and password, no header needed.")
    @Suppress("unused")
    suspend fun register(
        @GraphQLDescription(AuthInputDesc.MODEL)
        authInput: AuthInput,
    ): AuthResponse {
        if (!inputValidator.isValidEmail(authInput.email)) {
            throw GraphQLException(ErrorMessage.INVALID_EMAIL)
        }
        if (!inputValidator.isValidPassword(authInput.password)) {
            throw GraphQLException(ErrorMessage.INVALID_PASSWORD)
        }

        userCollection.find(Filters.eq(User::email.name, authInput.email)).firstOrNull()?.let {
            error(ErrorMessage.EMAIL_IN_USE)
        }

        val salt = generateSalt()
        var newUser = User(
            id = ObjectId(),
            email = authInput.email,
            passwordHash = authInput.password.encrypt(salt = salt),
            conversationIds = emptyList(),
            passwordSalt = salt,
            details = PersonalDetails(
                name = "Hannah Doe",
                phone = "984321765",
                dob = "2001-01-01",
                age = 21,
                job = "Teacher",
                education = Education.MASTERS,
                gender = Gender.FEMALE,
                currentLoc = Location(
                    geo = Point(Position(kmToLatitudeDegrees(90.0), 0.0)),
                    city = "Manchester",
                    country = "United Kingdom",
                    state = "England",
                ),
                liveLoc = SimpleLoc(
                    city = "Manchester",
                    country = "United Kingdom",
                ),
                birthLoc = SimpleLoc(
                    city = "Manchester",
                    country = "United Kingdom",
                ),
            ),
            profile = Profile(
                bio = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec euismod, nisl eget ultricies aliquam, nunc nisl aliquet nunc",
                pictures = listOf(
                    "https://picsum.photos/200/300",
                    "https://picsum.photos/200/300",
                    "https://picsum.photos/200/300",
                ),
                interests = Interests(
                    sports = listOf(Sports.BASKETBALL, Sports.FOOTBALL),
                    culinaries = listOf(),
                    creativities = listOf(),
                    leisures = listOf(),
                    socials = listOf(),
                    technologies = listOf(),
                    natures = listOf(),
                    minds = listOf(),
                    genders = listOf(),
                ),
                openQuestion = OpenQuestion.ACHIEVEMENT,
                aboutMe = AboutMe(
                    languages = listOf(Language.Spanish, Language.Polish),
                    height = 156,
                    fitness = Fitness.NEVER,
                    education = Education.MASTERS,
                    smoking = Smoking.TRYING_TO_QUIT,
                    drinking = Drinking.HEAVY_DRINKER,
                    personality = Personality.ESFJ,
                    loveLanguage = LoveLanguage.ACTS_OF_SERVICE,
                    children = Children.I_HAVE_AND_DONT_WANT_MORE,
                    relationship = Relationship.I_DONT_MIND,
                    diet = Diet.KOSHER,
                    pets = Pet.BIRDS,
                    zodiac = Zodiac.LIBRA,
                    politics = Politics.NON_POLITICAL,
                    religion = Religion.HINDUISM,
                ),
            ),
            updatedAt = "20L",
            verification = Verification(
                photoVerified = false,
                emailVerified = true,
                phoneVerified = true,
            ),
            profileCompletion = 85.0,
            isPremium = true,
            swipes = Swipes(),
        )

        val profileCompletion = newUser.checkProfileCompletion()
        newUser = newUser.copy(profileCompletion = profileCompletion)

        val result = userCollection.insertOne(newUser)

        return if (result.wasAcknowledged()) {
            createGeoIndexes()
            val token = jwtService.generateToken(newUser.id)
            AuthResponse(token, newUser.toMinimal())
        } else {
            throw GraphQLException(ErrorMessage.NOT_CREATED)
        }
    }

    private fun createGeoIndexes() {
        CoroutineScope(Dispatchers.IO).launch {
            userCollection.createIndex(
                Indexes.geo2dsphere(
                    "${User::details.name}.${PersonalDetails::currentLoc.name}.${Location::geo.name}",
                ),
            )
        }
    }
}
