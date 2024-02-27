package com.ajlabs.forevely.usecase.debug

import com.ajlabs.forevely.domain.util.encrypt
import com.ajlabs.forevely.domain.util.generateSalt
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
import com.ajlabs.forevely.model.user.Sports
import com.ajlabs.forevely.model.user.Swipes
import com.ajlabs.forevely.model.user.Technology
import com.ajlabs.forevely.model.user.User
import com.ajlabs.forevely.model.user.Verification
import com.ajlabs.forevely.model.user.Zodiac
import com.ajlabs.forevely.model.user.checkProfileCompletion
import com.mongodb.client.model.geojson.Point
import com.mongodb.client.model.geojson.Position
import org.bson.types.ObjectId

fun getGeneratedUsers(total: Int = 100): List<User> {
    require(total > 0)
    val users = (1..total).map {
        val salt1 = generateSalt()
        val gender = Gender.entries.random()
        val job = listOfJobTitles.random()
        val interests = Interests(
            sports = Sports.entries.randomList(),
            culinaries = Culinary.entries.randomList(),
            creativities = Creative.entries.randomList(),
            leisures = Leisure.entries.randomList(),
            socials = Social.entries.randomList(),
            technologies = Technology.entries.randomList(),
            natures = Nature.entries.randomList(),
            minds = Mind.entries.randomList(),
            genders = Gender.entries.randomList(),
        )
        val currentLocation = mapOfCountriesAndCities.entries.random()
        val currentLocationCity = currentLocation.value.random()
        val birthLocation = mapOfCountriesAndCities.entries.random()
        val birthLocationCity = birthLocation.value.random()
        val livesInLocation = mapOfCountriesAndCities.entries.random()
        val livesInLocationCity = livesInLocation.value.random()
        var user = User(
            id = ObjectId(),
            email = "test$it@test.com",
            passwordHash = "P@ss1234".encrypt(salt1),
            passwordSalt = salt1,
            profileCompletion = 95.0,
            isPremium = randomBoolean(),
            updatedAt = ((now() - 604800000)..now()).random().toString(),
            details = PersonalDetails(
                name = randomName(gender),
                phone = (1000000000..9999999999).random().toString(),
                dob = "${(1..28).random()}/${(1..12).random()}/${(1970..2002).random()}",
                age = (18..99).randomOrNull(),
                job = listOfJobTitles.randomOrNull(),
                education = Education.entries.randomOrNull(),
                gender = gender,
                currentLoc = Location(
                    geo = Point(Position(currentLocationCity.longitude, currentLocationCity.latitude)),
                    city = currentLocationCity.city,
                    country = currentLocation.key,
                    state = currentLocationCity.state,
                ),
                liveLoc = SimpleLoc(
                    city = livesInLocationCity.city,
                    country = livesInLocation.key,
                ),
                birthLoc = SimpleLoc(
                    city = birthLocationCity.city,
                    country = birthLocation.key,
                ),
            ),
            profile = Profile(
                openQuestion = OpenQuestion.entries.random(),
                bio = "I am a $job. and my interests are $interests",
                pictures = randomPictures(gender),
                interests = interests,
                aboutMe = AboutMe(
                    languages = Language.entries.randomList(),
                    height = (140..200).randomOrNull(),
                    fitness = Fitness.entries.randomOrNull(),
                    education = Education.entries.randomOrNull(),
                    smoking = Smoking.entries.randomOrNull(),
                    drinking = Drinking.entries.randomOrNull(),
                    personality = Personality.entries.randomOrNull(),
                    loveLanguage = LoveLanguage.entries.randomOrNull(),
                    children = Children.entries.randomOrNull(),
                    relationship = Relationship.entries.randomOrNull(),
                    diet = Diet.entries.randomOrNull(),
                    pets = Pet.entries.randomOrNull(),
                    zodiac = Zodiac.entries.randomOrNull(),
                    politics = Politics.entries.randomOrNull(),
                    religion = Religion.entries.randomOrNull(),
                ),
            ),
            conversationIds = emptyList(),
            verification = Verification(
                photoVerified = randomBoolean(),
                emailVerified = randomBoolean(),
                phoneVerified = randomBoolean(),
            ),
            swipes = Swipes(),
        )
        user = user.copy(
            profileCompletion = user.checkProfileCompletion(),
            details = user.details.copy(
                name = randomName(gender),
            ),
        )
        user
    }

    val allIds = users.map { it.id }

    return users.map { user ->
        val likes = allIds.randomList((total * 0.2).toInt(), (total * 0.5).toInt())
        val swipes = Swipes(likes, listOf())
        user.copy(swipes = swipes)
    }
}

data class GenerateUsersResult(
    val before: Int,
    val after: Int,
    val inserted: Int,
)

private fun randomBoolean() = (0..1).random() == 1

private fun randomName(gender: Gender): String = when (gender) {
    Gender.MALE -> listOfMaleNames.random()
    Gender.FEMALE -> listOfFemaleNames.random()
    else -> listOfUnisexNames.random()
}

private fun randomPictures(gender: Gender): List<String> {
    return when (gender) {
        Gender.MALE -> maleImages
        Gender.FEMALE -> maleImages
        else -> (maleImages + femalePictures)
    }.randomList(3, 6)
}

private fun <T> Collection<T>.randomOrNull(): T? {
    val listWithNull = this.toMutableList() + null
    return listWithNull.random()
}

private fun now() = System.currentTimeMillis()

fun <T> Collection<T>.randomList(min: Int = 0, max: Int = 2): List<T> {
    val total = (min..max).random()
    return (0..total).map { this.random() }
}

private val listOfMaleNames: List<String> = listOf(
    "James",
    "John",
    "Robert",
    "Michael",
    "William",
    "David",
    "Richard",
    "Joseph",
    "Charles",
    "Thomas",
    "Christopher",
    "Daniel",
    "Matthew",
    "Anthony",
    "Mark",
    "Donald",
    "Steven",
    "Paul",
    "Andrew",
    "Joshua",
    "Kenneth",
    "Kevin",
    "Brian",
    "George",
    "Edward",
    "Ronald",
    "Timothy",
    "Jason",
    "Jeffrey",
    "Ryan",
    "Jacob",
    "Gary",
    "Nicholas",
    "Eric",
    "Stephen",
    "Jonathan",
    "Larry",
    "Justin",
    "Scott",
    "Brandon",
    "Benjamin",
    "Samuel",
    "Gregory",
    "Alexander",
    "Frank",
    "Raymond",
    "Patrick",
    "Jack",
    "Dennis",
    "Jerry",
)

private val listOfFemaleNames: List<String> = listOf(
    "Mary",
    "Patricia",
    "Jennifer",
    "Linda",
    "Elizabeth",
    "Barbara",
    "Susan",
    "Jessica",
    "Sarah",
    "Karen",
    "Nancy",
    "Lisa",
    "Margaret",
    "Betty",
    "Sandra",
    "Ashley",
    "Dorothy",
    "Kimberly",
    "Emily",
    "Donna",
    "Michelle",
    "Carol",
    "Amanda",
    "Melissa",
    "Deborah",
    "Stephanie",
    "Rebecca",
    "Laura",
    "Sharon",
    "Cynthia",
    "Kathleen",
    "Amy",
    "Shirley",
    "Angela",
    "Helen",
    "Anna",
    "Brenda",
    "Pamela",
    "Nicole",
    "Samantha",
    "Katherine",
    "Emma",
    "Ruth",
    "Christine",
    "Catherine",
    "Debra",
    "Rachel",
    "Carolyn",
    "Janet",
    "Virginia",
)

private val listOfUnisexNames: List<String> = listOf(
    "Jordan",
    "Taylor",
    "Alex",
    "Casey",
    "Jamie",
    "Morgan",
    "Jesse",
    "Avery",
    "Riley",
    "Cameron",
    "Sam",
    "Quinn",
    "Peyton",
    "Skyler",
    "Charlie",
    "Dakota",
    "Emerson",
    "Reese",
    "Robin",
    "Adrian",
)

private val listOfJobTitles: List<String> = listOf(
    "Software Engineer",
    "Marketing Manager",
    "Graphic Designer",
    "Data Analyst",
    "Nurse",
    "Architect",
    "Teacher",
    "Accountant",
    "Web Developer",
    "Human Resources Manager",
    "Sales Representative",
    "Project Manager",
    "Chef",
    "Mechanical Engineer",
    "Journalist",
    "Dentist",
    "Pharmacist",
    "Civil Engineer",
    "Electrician",
    "Veterinarian",
    "Physiotherapist",
    "Event Planner",
    "Photographer",
    "Interior Designer",
    "Environmental Scientist",
    "Flight Attendant",
    "Chef",
    "Social Worker",
    "Biologist",
    "Economist",
)

private data class CityStateAndCoordinates(
    val city: String,
    val state: String,
    val latitude: Double,
    val longitude: Double,
)

private val mapOfCountriesAndCities: Map<String, List<CityStateAndCoordinates>> = mapOf(
    "United States" to listOf(
        CityStateAndCoordinates("New York", "New York", 40.6973709, -74.1444828),
        CityStateAndCoordinates("Los Angeles", "California", 34.0100872, -118.2437),
        CityStateAndCoordinates("Chicago", "Illinois", 41.8781, -87.6298),
        CityStateAndCoordinates("Houston", "Texas", 29.7604, -95.3698),
        CityStateAndCoordinates("Phoenix", "Arizona", 33.4484, -112.0740),
    ),
    "United Kingdom" to listOf(
        CityStateAndCoordinates("London", "England", 51.5074, -0.1278),
        CityStateAndCoordinates("Edinburgh", "Scotland", 55.9533, -3.1883),
        CityStateAndCoordinates("Cardiff", "Wales", 51.4816, -3.1791),
        CityStateAndCoordinates("Belfast", "Northern Ireland", 54.594852, -6.0090311),
        CityStateAndCoordinates("Manchester", "England", 53.4808, -2.2426),
    ),
    "France" to listOf(
        CityStateAndCoordinates("Paris", "Île-de-France", 48.8566, 2.3522),
        CityStateAndCoordinates("Marseille", "Provence-Alpes-Côte d'Azur", 43.2965, 5.3698),
        CityStateAndCoordinates("Lyon", "Auvergne-Rhône-Alpes", 45.7640, 4.8357),
        CityStateAndCoordinates("Toulouse", "Occitanie", 43.6047, 1.4442),
        CityStateAndCoordinates("Nice", "Provence-Alpes-Côte dAzur", 43.7102, 7.2620),
    ),
    "Spain" to listOf(
        CityStateAndCoordinates("Madrid", "Madrid", 40.4168, -3.7038),
        CityStateAndCoordinates("Barcelona", "Catalonia", 41.3851, 2.1734),
        CityStateAndCoordinates("Valencia", "Valencia", 39.4699, -0.3763),
        CityStateAndCoordinates("Seville", "Andalusia", 37.3891, -5.9845),
        CityStateAndCoordinates("Bilbao", "Basque Country", 43.2630, -2.9350),
    ),
)

private val femalePictures = listOf(
    "https://img.freepik.com/free-photo/charming-girl-stands-street_8353-5373.jpg?w=900&t=st=1700158995~exp=1700159595~hmac=1f7b86449feaf8204f28bd9dfbbf4b7ea7014b9533e656a4865f4838ef65c388",
    "https://img.freepik.com/free-photo/attractive-stylish-student-girl-with-backpack-confidently-looking-camera-city-street_574295-1337.jpg?w=900&t=st=1700159034~exp=1700159634~hmac=e64752c8a78412fb0f670f1978109a788311ca97ed6565c6d93bac19045e2c7c",
    "https://img.freepik.com/free-photo/beautiful-girl-stands-park_8353-5084.jpg?w=900&t=st=1700159050~exp=1700159650~hmac=8d8c39eb248c55575e93039368fbf1f82b3ad07ccbea9def5913cadaadda44ad",
    "https://img.freepik.com/premium-photo/young-stylish-blonde-drinking-coffee-go-smiling-walking-along-street_341862-10316.jpg?w=900",
    "https://img.freepik.com/free-photo/attractive-girl-unusual-skirt_158595-25.jpg?w=1380&t=st=1700159075~exp=1700159675~hmac=ac502d107dfccca170d78e2e23af5071ae64ca732d4964348ac586fca1c5b2ca",
    "https://img.freepik.com/free-photo/sexy-beautiful-woman-beach_144627-18222.jpg?w=900&t=st=1700159126~exp=1700159726~hmac=2c831231a5ddd233baa6d3622c5910518b70fa0a8b8d7658aaa4e3114bd3062a",
    "https://img.freepik.com/free-photo/portrait-summer-girl-white-hat_329181-4468.jpg?w=900&t=st=1700159150~exp=1700159750~hmac=cecdfb0ec8b03d99b5fbadf5b125750d1ea95b74e2861916573404860976ef26",
    "https://img.freepik.com/free-photo/young-beautiful-slender-woman-swimsuit-beach-tropical-island_1321-2566.jpg?w=900&t=st=1700159175~exp=1700159775~hmac=84ae82303084e8cd8516b46cb822ffa768be7568228f08473372cc9280626300",
    "https://img.freepik.com/premium-photo/beautiful-slim-sexy-woman-bikini-sitting-beach_358320-6973.jpg?w=900",
    "https://img.freepik.com/free-photo/pretty-slim-woman-playing-with-watergun-toy-pool-summer-tropical-vacation-villa-hotel-having-fun-bikini-swimsuit-colorful-style-party-mood-beautiful-skinny-fit-body_285396-3943.jpg?w=900&t=st=1700159215~exp=1700159815~hmac=ea0eda157e27a773e0d122aab0d344b400e7a36493dd31d40b19deb9b1bfa676",
    "https://img.freepik.com/premium-photo/beautiful-blonde-woman-yacht-wearing-sexy-red-bikini-red-sunglasses_120960-153.jpg?w=900",
    "https://img.freepik.com/free-photo/portrait-young-businesswoman-holding-eyeglasses-hand-against-gray-backdrop_23-2148029483.jpg?w=1380&t=st=1700159277~exp=1700159877~hmac=95b3c43f884bc5433aea6444bbfbb0bcbb6e6022dfac959ce151d1253a2dde1e",
    "https://img.freepik.com/free-photo/portrait-lovely-young-woman-forest-caucasian-female-woman-with-dark-hair-green-eyes-smiling-camera-portrait-nature-beauty-concept_74855-23456.jpg?w=900&t=st=1700159289~exp=1700159889~hmac=31acfd240748e0b300d3597610f752816ed8a3cfccf3b5e231fd7a14263822c5",
    "https://img.freepik.com/free-photo/beautiful-woman-bright-sweater-posing-street-against-background-sakura-city-portrait-attractive-lady-yellow-outfit-smiling-widely_197531-17887.jpg?w=900&t=st=1700159307~exp=1700159907~hmac=772b370e2d664fe09e514a1f462c6d10816fc2dc16bdd045a33b59f9b6666f36",
    "https://img.freepik.com/free-photo/portrait-young-woman-with-natural-make-up_23-2149084942.jpg?w=900&t=st=1700159319~exp=1700159919~hmac=ce9e69e10d9bf50b89583197df5405a2209d90c86b607138d68d335e27adebbc",
    "https://img.freepik.com/free-photo/wavy-haired-woman-light-shirt-with-black-lace-sniffing-flower-city-tender-woman-with-red-lips-short-hair-poses-street_197531-19308.jpg?w=900&t=st=1700159347~exp=1700159947~hmac=2f0d6c0cc042972258c56677b745e523006689376c0d459a8b0e73e75bd1d8e4",
    "https://img.freepik.com/free-photo/vertical-shot-beautiful-smiling-woman-outdoors_181624-39350.jpg?w=900&t=st=1700159359~exp=1700159959~hmac=861ccd62acffd16f86cbf69c7c6deb8daaec9b096357a337f22d7eb802c4c253",
    "https://img.freepik.com/free-photo/portrait-young-blonde-woman-with-with-tanned-skin-fashion-clothing_144627-47356.jpg?w=900&t=st=1700159368~exp=1700159968~hmac=b340e7ce5f820835a5797f0c1aed3f2386522e112b5a354a372c5f6710895b92",
    "https://img.freepik.com/free-photo/medium-shot-woman-posing-outdoors_23-2150725945.jpg?w=900&t=st=1700159380~exp=1700159980~hmac=f0ad30465057bd8f2c845fca6eac643ade04fe57ad19ce4b1375207ca03328af",
    "https://img.freepik.com/premium-photo/portrait-woman-with-rose-flowers-her-hair-natural-beauty-facial-skin-hair-care-strong-hair-woman-beauty-face_91497-8304.jpg?w=996",
    "https://img.freepik.com/free-photo/young-lady-put-her-hand-her-arms-looking-camera-white-background-high-quality-photo_144627-74854.jpg?w=900&t=st=1700159404~exp=1700160004~hmac=3aaf599660cb74650cad45f44372dc1bf3290f53523831855083bde248ebec47",
    "https://img.freepik.com/free-photo/happy-woman-home-during-coronavirus-quarantine_53876-137722.jpg?w=1380&t=st=1700159494~exp=1700160094~hmac=cdfeb85ec921cddc1993f2fe468f9bed80705a43feb6b65b30ff4bf61b0684f2",
    "https://img.freepik.com/free-photo/medium-shot-woman-posing-outdoors_23-2150725943.jpg?w=900&t=st=1700159510~exp=1700160110~hmac=151cec9a1cb77fa7fce0c89ca13bf33752ab8c78ce00f6579458b9713c018966",
    "https://img.freepik.com/free-photo/indoor-studio-shot-attractive-pretty-woman-with-light-brown-hair-wearing-black-jacket-with-red-lips_291650-1321.jpg?w=900&t=st=1700159525~exp=1700160125~hmac=d8c696c569ad7b457b07a4bff7ed9754ceac47c36f8cc0c3d65914f8baef75dc",
    "https://img.freepik.com/free-photo/snapshot-curly-blond-girl-with-slight-smile-looking-camera-street_197531-26018.jpg?w=1380&t=st=1700159550~exp=1700160150~hmac=b1ee1ce88bd063a3aa0a7646f98c4804e923fa7b90aea3bfde0ca657aa7fa075",
)

private val maleImages = listOf(
    "https://img.freepik.com/free-photo/cheerful-smiling-sportsman-activewear-is-relaxing-bench-park_613910-2750.jpg?w=900&t=st=1700159592~exp=1700160192~hmac=c85674a553e75185b73e79b8f14662c47f2b3557df625ee95c69dd368cd13841",
    "https://img.freepik.com/free-photo/artist-white_1368-3546.jpg?w=1380&t=st=1700159606~exp=1700160206~hmac=7eda42bca2b6c8934afd7c78722bd5165f7992dd1d31475251fab3d3e9763cc9",
    "https://img.freepik.com/free-photo/smiley-man-relaxing-outdoors_23-2148739334.jpg?w=900&t=st=1700159616~exp=1700160216~hmac=87d42ef4e249c0a100cbe68bc533c957ea4486b3e65a000952c07348de9a91f6",
    "https://img.freepik.com/free-photo/handsome-smiling-young-man-sitting-office-coworking_171337-17642.jpg?w=1380&t=st=1700159626~exp=1700160226~hmac=1a21d298f9b273488f50b45da0d7387f2d1de349830b4e3f2f9080a8b110a0e7",
    "https://img.freepik.com/free-photo/portrait-white-man-isolated_53876-40306.jpg?w=1800&t=st=1700159645~exp=1700160245~hmac=739a4a011d59a79dd8d5464815e8472ba56edccbb43950e0dbd029ceae6fb90e",
    "https://img.freepik.com/free-photo/middle-aged-man-wearing-leaning-against-rusty-colored-background_150588-73.jpg?w=900&t=st=1700159660~exp=1700160260~hmac=288341cf930696b4e376fe836bd04b301fb341298b196284004d8374435213f1",
    "https://img.freepik.com/free-photo/fashion-portrait-young-man-black-shirt-poses-wall-with-contrast-shadows_186202-4522.jpg?w=1380&t=st=1700159689~exp=1700160289~hmac=aa22540284b72e1464ac08312cd2884a2653b0e3de0ded3e57c5e1be418c05a5",
    "https://img.freepik.com/free-photo/confident-young-man-stylish-jacket-looking-camera-outdoors-generated-by-ai_24640-130952.jpg?w=1380&t=st=1700159705~exp=1700160305~hmac=b8c2437a43884655ef83d9f61fe9491f6a29876a0b34f674e09825ca81d2f2d2",
    "https://img.freepik.com/free-photo/portrait-modern-man_23-2147960990.jpg?w=1380&t=st=1700159738~exp=1700160338~hmac=face0573ac94062698cc51a0497bd36dccbfab220ad790ca3ae25105d348cea6",
    "https://img.freepik.com/free-photo/handsome-young-male-walking-street_158595-4724.jpg?w=900&t=st=1700160106~exp=1700160706~hmac=e8027b37c008fcd08a5f7ef2e13cbe805412de2ad05252d7c97ce6e051309d69",
    "https://img.freepik.com/free-photo/close-up-sad-man_23-2150896109.jpg?t=st=1700160139~exp=1700163739~hmac=5aa6c2530e0addd4f487461c4226ccbbebf3e194dcc3f9962336768708aaaf51&w=826",
    "https://img.freepik.com/premium-photo/portrait-hipster-man-with-fashionable-hair-beard_100800-11052.jpg?w=900",
    "https://img.freepik.com/free-photo/full-length-portrait-handsome-successful-businessman_171337-18653.jpg?w=900&t=st=1700160189~exp=1700160789~hmac=c146232da83f041f6f97d474b305ac245db7321814b78ea844793ceb1d0a7a0b",
    "https://img.freepik.com/premium-photo/handsome-asian-man-stand-smile-posing-gray-background_264197-21216.jpg?w=900",
)
