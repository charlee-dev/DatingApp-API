package com.ajlabs.forevely.mock

import com.ajlabs.forevely.domain.util.encrypt
import com.ajlabs.forevely.domain.util.generateSalt
import com.ajlabs.forevely.domain.util.kmToLatitudeDegrees
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
import com.ajlabs.forevely.model.user.PersonalDetails
import com.ajlabs.forevely.model.user.Personality
import com.ajlabs.forevely.model.user.Pet
import com.ajlabs.forevely.model.user.Politics
import com.ajlabs.forevely.model.user.Profile
import com.ajlabs.forevely.model.user.Relationship
import com.ajlabs.forevely.model.user.Religion
import com.ajlabs.forevely.model.user.SimpleLoc
import com.ajlabs.forevely.model.user.Smoking
import com.ajlabs.forevely.model.user.Social
import com.ajlabs.forevely.model.user.Sports.BASKETBALL
import com.ajlabs.forevely.model.user.Swipes
import com.ajlabs.forevely.model.user.Technology
import com.ajlabs.forevely.model.user.User
import com.ajlabs.forevely.model.user.Verification
import com.ajlabs.forevely.model.user.Zodiac
import com.mongodb.client.model.geojson.Point
import com.mongodb.client.model.geojson.Position
import org.bson.types.ObjectId

const val PASS_1 = "Password123"
const val PASS_2 = "Password456"

val salt1 = generateSalt()
val user1 = User(
    id = ObjectId(),
    email = "user1@gmail.com",
    passwordHash = PASS_1.encrypt(salt1),
    passwordSalt = salt1,
    profileCompletion = 95.0,
    isPremium = false,
    updatedAt = "0L",
    details = PersonalDetails(
        name = "John Smith",
        phone = "123456789",
        dob = "01/01/2000",
        age = 20,
        job = "Software Engineer",
        education = Education.BACHELORS,
        gender = Gender.MALE,
        currentLoc = Location(
            geo = Point(Position(0.0, 0.0)),
            city = "London",
            country = "United Kingdom",
            state = "England",
        ),
        liveLoc = SimpleLoc(
            city = "London",
            country = "United Kingdom",
        ),
        birthLoc = SimpleLoc(
            city = "London",
            country = "United Kingdom",
        ),
    ),
    profile = Profile(
        bio = "",
        pictures = listOf(),
        interests = Interests(
            sports = listOf(BASKETBALL),
            culinaries = listOf(Culinary.VEGETARIAN_CUISINE),
            creativities = listOf(Creative.POTTERY),
            leisures = listOf(Leisure.PUZZLES),
            socials = listOf(Social.BOOK_CLUB),
            technologies = listOf(Technology.ROBOTICS),
            natures = listOf(Nature.GEOCACHING),
            minds = listOf(Mind.TAROT),
            genders = listOf(Gender.FEMALE, Gender.MALE),
        ),
        aboutMe = AboutMe(
            languages = listOf(Language.Polish, Language.Spanish),
            height = 186,
            fitness = Fitness.OFTEN,
            education = Education.STUDYING_POSTGRADUATE,
            smoking = Smoking.NON_SMOKER,
            drinking = Drinking.HEAVY_DRINKER,
            personality = Personality.ENFJ,
            loveLanguage = LoveLanguage.ACTS_OF_SERVICE,
            children = Children.I_HAVE_AND_DONT_WANT_MORE,
            relationship = Relationship.CONTEMPORARY,
            diet = Diet.CARNIVORE,
            pets = Pet.BIRDS,
            zodiac = Zodiac.AQUARIUS,
            politics = Politics.CENTER,
            religion = Religion.ATHEISM,
        ),
    ),
    conversationIds = emptyList(),
    verification = Verification(
        photoVerified = true,
        emailVerified = false,
        phoneVerified = true,
    ),
    swipes = Swipes(),
)

val salt2 = generateSalt()
val user2 = User(
    id = ObjectId(),
    email = "user2@gmail.com",
    passwordHash = PASS_1.encrypt(salt2),
    passwordSalt = salt2,
    conversationIds = emptyList(),
    details = PersonalDetails(
        name = "Jane Smith",
        phone = "987654321",
        dob = "01/01/2000",
        age = 31,
        job = "Baker",
        education = Education.BACHELORS,
        gender = Gender.FEMALE,
        currentLoc = Location(
            // Epsom Toyota GB
            geo = Point(Position(kmToLatitudeDegrees(10.0), 0.0)),
            city = "Epsom",
            country = "United Kingdom",
            state = "England",
        ),
        liveLoc = SimpleLoc(
            city = "Epsom",
            country = "United Kingdom",
        ),
        birthLoc = SimpleLoc(
            city = "Epsom",
            country = "United Kingdom",
        ),
    ),
    profile = Profile(
        bio = "I am a baker",
        pictures = listOf(),
        interests = Interests(
            sports = listOf(BASKETBALL),
            culinaries = listOf(Culinary.MIXOLOGY),
            creativities = listOf(Creative.PLAYING_AN_INSTRUMENT),
            leisures = listOf(Leisure.BOARD_GAMES),
            socials = listOf(Social.CONCERTS),
            technologies = listOf(Technology.CODING),
            natures = listOf(Nature.ENVIRONMENTAL_ACTIVISM),
            minds = listOf(Mind.PHILOSOPHICAL_DEBATES),
            genders = listOf(Gender.FEMALE),
        ),
        aboutMe = AboutMe(
            languages = listOf(Language.Polish, Language.English),
            height = 106,
            fitness = Fitness.SOMETIMES,
            education = Education.PHD,
            smoking = Smoking.SMOKER_WHEN_DRINKING,
            drinking = Drinking.REGULAR_DRINKER,
            personality = Personality.INFJ,
            loveLanguage = LoveLanguage.PHYSICAL_TOUCH,
            children = Children.I_HAVE_AND_WANT_MORE,
            relationship = Relationship.I_DONT_MIND,
            diet = Diet.HALAL,
            pets = Pet.DOGS,
            zodiac = Zodiac.ARIES,
            politics = Politics.RIGHT_WING,
            religion = Religion.CHRISTIANITY,
        ),
    ),
    updatedAt = "0L",
    verification = Verification(
        photoVerified = false,
        emailVerified = false,
        phoneVerified = true,
    ),
    profileCompletion = 10.0,
    isPremium = true,
    swipes = Swipes(),
)

val salt3 = generateSalt()
val user3 = User(
    id = ObjectId(),
    email = "user3@gmail.com",
    passwordHash = PASS_2.encrypt(salt3),
    passwordSalt = salt3,
    conversationIds = emptyList(),
    details = PersonalDetails(
        name = "John Doe",
        phone = "987654321",
        dob = "01/01/2000",
        age = 41,
        job = "Driver",
        education = Education.PHD,
        gender = Gender.FEMALE,
        currentLoc = Location(
            geo = Point(Position(kmToLatitudeDegrees(50.0), 0.0)),
            city = "Cambridge",
            country = "United Kingdom",
            state = "England",
        ),
        liveLoc = SimpleLoc(
            city = "Cambridge",
            country = "United Kingdom",
        ),
        birthLoc = SimpleLoc(
            city = "Cambridge",
            country = "United Kingdom",
        ),
    ),
    profile = Profile(
        bio = "I am a driver",
        pictures = listOf(),
        interests = Interests(
            sports = listOf(BASKETBALL),
            culinaries = listOf(Culinary.BARBECUING),
            creativities = listOf(Creative.KNITTING),
            leisures = listOf(Leisure.FISHING),
            socials = listOf(Social.OPERA),
            technologies = listOf(Technology.DRONE_PILOTING),
            natures = listOf(Nature.NATURE_PHOTOGRAPHY),
            minds = listOf(Mind.ASTROLOGY),
            genders = listOf(Gender.NON_BINARY),
        ),
        aboutMe = AboutMe(
            languages = listOf(Language.Spanish),
            height = 146,
            fitness = Fitness.OFTEN,
            education = Education.HIGH_SCHOOL,
            smoking = Smoking.SOCIAL_SMOKER,
            drinking = Drinking.SOBER,
            personality = Personality.INTJ,
            loveLanguage = LoveLanguage.RECEIVING_GIFTS,
            children = Children.I_HAVE_AND_WANT_MORE,
            relationship = Relationship.TRADITIONAL,
            diet = Diet.VEGETARIAN,
            pets = Pet.CATS,
            zodiac = Zodiac.SAGITTARIUS,
            politics = Politics.LEFT_WING,
            religion = Religion.ISLAM,
        ),
    ),
    updatedAt = "0L",
    verification = Verification(
        photoVerified = false,
        emailVerified = true,
        phoneVerified = true,
    ),
    profileCompletion = 85.0,
    isPremium = false,
    swipes = Swipes(),
)

val salt4 = generateSalt()
val user4 = User(
    id = ObjectId(),
    email = "user4@gmail.com",
    passwordHash = PASS_2.encrypt(salt4),
    passwordSalt = salt4,
    conversationIds = emptyList(),
    details = PersonalDetails(
        name = "Hannah Doe",
        phone = "984321765",
        dob = "03/04/2001",
        age = 61,
        job = "Teacher",
        education = Education.PHD,
        gender = Gender.NON_BINARY,
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
        bio = "I am a teacher",
        pictures = listOf(),
        interests = Interests(
            sports = listOf(BASKETBALL),
            culinaries = listOf(Culinary.BAKING),
            creativities = listOf(Creative.DANCING),
            leisures = listOf(Leisure.BIRD_WATCHING),
            socials = listOf(Social.SOCIAL_ACTIVISM),
            technologies = listOf(Technology.PODCASTING),
            natures = listOf(Nature.GARDENING),
            minds = listOf(Mind.MINDFULNESS),
            genders = listOf(Gender.NON_BINARY),
        ),
        aboutMe = AboutMe(
            languages = listOf(Language.Arabic, Language.Spanish, Language.Polish),
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

val mockUsers = listOf(user1, user2, user3, user4)
