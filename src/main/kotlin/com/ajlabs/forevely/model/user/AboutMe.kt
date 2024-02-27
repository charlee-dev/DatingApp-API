package com.ajlabs.forevely.model.user

import com.ajlabs.forevely.model.Language
import com.expediagroup.graphql.generator.annotations.GraphQLDescription

@GraphQLDescription(AboutMeDesc.MODEL)
data class AboutMe(
    @GraphQLDescription(AboutMeDesc.LANGUAGES)
    val languages: List<Language> = emptyList(),
    @GraphQLDescription(AboutMeDesc.HEIGHT)
    val height: Int? = null,
    @GraphQLDescription(AboutMeDesc.FITNESS)
    val fitness: Fitness? = null,
    @GraphQLDescription(AboutMeDesc.EDUCATION)
    val education: Education? = null,
    @GraphQLDescription(AboutMeDesc.SMOKING)
    val smoking: Smoking? = null,
    @GraphQLDescription(AboutMeDesc.DRINKING)
    val drinking: Drinking? = null,
    @GraphQLDescription(AboutMeDesc.PERSONALITY)
    val personality: Personality? = null,
    @GraphQLDescription(AboutMeDesc.LOVE_LANGUAGE)
    val loveLanguage: LoveLanguage? = null,
    @GraphQLDescription(AboutMeDesc.CHILDREN)
    val children: Children? = null,
    @GraphQLDescription(AboutMeDesc.RELATIONSHIP)
    val relationship: Relationship? = null,
    @GraphQLDescription(AboutMeDesc.DIET)
    val diet: Diet? = null,
    @GraphQLDescription(AboutMeDesc.PETS)
    val pets: Pet? = null,
    @GraphQLDescription(AboutMeDesc.ZODIAC)
    val zodiac: Zodiac? = null,
    @GraphQLDescription(AboutMeDesc.POLITICS)
    val politics: Politics? = null,
    @GraphQLDescription(AboutMeDesc.RELIGION)
    val religion: Religion? = null,
)

object AboutMeDesc {
    const val MODEL = "Represents a user's about me section."
    const val LANGUAGES = "Languages the user speaks."
    const val HEIGHT = "Height of the user."
    const val FITNESS = "Fitness status of the user."
    const val EDUCATION = "Education level of the user."
    const val SMOKING = "Smoking status of the user."
    const val DRINKING = "Drinking status of the user."
    const val PERSONALITY = "Personality type of the user."
    const val LOVE_LANGUAGE = "Love language of the user."
    const val CHILDREN = "Children preference of the user."
    const val RELATIONSHIP = "Relationship preference of the user."
    const val DIET = "Dietary preferences of the user."
    const val PETS = "Pet preferences of the user."
    const val ZODIAC = "Zodiac sign of the user."
    const val POLITICS = "Political views of the user."
    const val RELIGION = "Religion of the user."
}

enum class Pet {
    DOGS, CATS, BIRDS, REPTILES, FISH, OTHER
}

enum class Diet {
    OMNIVORE, VEGAN, VEGETARIAN, PESCATARIAN, CARNIVORE, KOSHER, HALAL, OTHER
}

enum class Children {
    I_HAVE_AND_WANT_MORE, I_HAVE_AND_DONT_WANT_MORE
}

enum class LoveLanguage {
    WORDS_OF_AFFIRMATION, QUALITY_TIME, RECEIVING_GIFTS, ACTS_OF_SERVICE, PHYSICAL_TOUCH
}

enum class Personality {
    INTJ, INTP, ENTJ, ENTP, INFJ, INFP, ENFJ, ENFP, ISTJ, ISFJ, ESTJ, ESFJ, ISTP, ISFP, ESTP, ESFP
}

enum class Fitness {
    EVERYDAY, OFTEN, SOMETIMES, RARELY, NEVER
}

enum class Education {
    HIGH_SCHOOL, STUDYING_BACHELORS, BACHELORS, STUDYING_POSTGRADUATE, MASTERS, PHD
}

enum class Smoking {
    NON_SMOKER, SOCIAL_SMOKER, SMOKER_WHEN_DRINKING, REGULAR_SMOKER, TRYING_TO_QUIT
}

enum class Drinking {
    RARELY, SOCIAL_DRINKER, REGULAR_DRINKER, HEAVY_DRINKER, THINKING_ABOUT_QUITTING, SOBER
}

enum class Relationship {
    TRADITIONAL, CONTEMPORARY, I_DONT_MIND
}

enum class Zodiac {
    ARIES, TAURUS, GEMINI, CANCER, LEO, VIRGO, LIBRA, SCORPIO, SAGITTARIUS, CAPRICORN, AQUARIUS, PISCES
}

enum class Politics {
    LEFT_WING, RIGHT_WING, CENTER, NON_POLITICAL
}

enum class Religion {
    CHRISTIANITY, ISLAM, HINDUISM, BUDDHISM, JUDAISM, SIKHISM, ATHEISM, OTHER
}

data class Interests(
    val sports: List<Sports>? = null,
    val culinaries: List<Culinary>? = null,
    val creativities: List<Creative>? = null,
    val leisures: List<Leisure>? = null,
    val socials: List<Social>? = null,
    val technologies: List<Technology>? = null,
    val natures: List<Nature>? = null,
    val minds: List<Mind>? = null,
    val genders: List<Gender>? = null,
) {
    fun size(): Int {
        return (sports?.size ?: 0) +
            (culinaries?.size ?: 0) +
            (creativities?.size ?: 0) +
            (leisures?.size ?: 0) +
            (socials?.size ?: 0) +
            (technologies?.size ?: 0) +
            (natures?.size ?: 0) +
            (minds?.size ?: 0) +
            (genders?.size ?: 0)
    }

    override fun toString(): String {
        return "${sports?.joinToString(", ")}, " +
            "${culinaries?.joinToString(", ")}, " +
            "${creativities?.joinToString(", ")}, " +
            "${leisures?.joinToString(", ")}, " +
            "${socials?.joinToString(", ")}, " +
            "${technologies?.joinToString(", ")}, " +
            "${natures?.joinToString(", ")}, " +
            "${minds?.joinToString(", ")}, " +
            "${genders?.joinToString(", ")}"
    }
}

enum class Sports {
    YOGA,
    BOXING,
    RUNNING,
    GYM_WORKOUTS,
    CYCLING,
    HIKING,
    SWIMMING,
    MARTIAL_ARTS,
    BASKETBALL,
    FOOTBALL,
    TENNIS,
    GOLF,
    SKIING,
    SNOWBOARDING,
    SURFING,
    ROCK_CLIMBING,
    ROWING,
    PILATES,
    SKATING,
}

enum class Culinary {
    COOKING,
    BAKING,
    WINE_TASTING,
    CRAFT_BEER,
    FOODIE_TOURS,
    BARBECUING,
    VEGAN_CUISINE,
    VEGETARIAN_CUISINE,
    MIXOLOGY,
    CHOCOLATE_MAKING,
}

enum class Creative {
    PAINTING,
    DRAWING,
    SCULPTING,
    PHOTOGRAPHY,
    THEATER,
    SINGING,
    PLAYING_AN_INSTRUMENT,
    WRITING,
    POTTERY,
    KNITTING,
    SEWING,
    DIY_CRAFTS,
    FILMMAKING,
    DANCING,
    STAND_UP_COMEDY,
}

enum class Leisure {
    SHOPPING,
    TRAVELING,
    FISHING,
    BOATING,
    BIRD_WATCHING,
    BOARD_GAMES,
    VIDEO_GAMING,
    WATCHING_MOVIES,
    READING,
    PUZZLES,
    COIN_COLLECTING,
    ANTIQUING,
    ASTRONOMY,
}

enum class Social {
    CONCERTS,
    MUSEUM_VISITING,
    THEATER_GOING,
    OPERA,
    BOOK_CLUB,
    LANGUAGE_LEARNING,
    SOCIAL_ACTIVISM,
    VOLUNTEERING,
    POLITICAL_CAMPAIGNING,
    ANIMAL_RESCUE,
}

enum class Technology {
    CODING,
    ROBOTICS,
    WEB_DESIGN,
    BLOGGING,
    PODCASTING,
    DRONE_PILOTING,
    THREEDEE_PRINTING,
    GADGETEERING,
    CYBERSECURITY,
}

enum class Nature {
    CAMPING,
    WILDLIFE_CONSERVATION,
    GARDENING,
    ENVIRONMENTAL_ACTIVISM,
    SUSTAINABLE_LIVING,
    HORSEBACK_RIDING,
    NATURE_PHOTOGRAPHY,
    SCUBA_DIVING,
    KAYAKING,
    SAILING,
    GEOCACHING,
}

enum class Mind {
    MEDITATION,
    TAROT,
    ASTROLOGY,
    PHILOSOPHICAL_DEBATES,
    PSYCHOLOGY,
    MINDFULNESS,
    SELF_IMPROVEMENT,
}

