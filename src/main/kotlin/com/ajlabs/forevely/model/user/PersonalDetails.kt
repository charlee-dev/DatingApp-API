package com.ajlabs.forevely.model.user

import com.expediagroup.graphql.generator.annotations.GraphQLDescription

@GraphQLDescription(PersonalDetailsDesc.MODEL)
data class PersonalDetails(
    @GraphQLDescription(PersonalDetailsDesc.NAME)
    val name: String? = null,
    @GraphQLDescription(PersonalDetailsDesc.PHONE)
    val phone: String? = null,
    @GraphQLDescription(PersonalDetailsDesc.DOB)
    val dob: String? = null,
    @GraphQLDescription(PersonalDetailsDesc.AGE)
    val age: Int? = null,
    @GraphQLDescription(PersonalDetailsDesc.JOB)
    val job: String? = null,
    @GraphQLDescription(PersonalDetailsDesc.EDUCATION)
    val education: Education? = null,
    @GraphQLDescription(PersonalDetailsDesc.GENDER)
    val gender: Gender? = null,
    @GraphQLDescription(PersonalDetailsDesc.CURRENT_LOC)
    val currentLoc: Location? = null,
    @GraphQLDescription(PersonalDetailsDesc.LIVE_LOC)
    val liveLoc: SimpleLoc? = null,
    @GraphQLDescription(PersonalDetailsDesc.BIRTH_LOC)
    val birthLoc: SimpleLoc? = null,
)

@GraphQLDescription(PersonalDetailsDesc.MODEL)
data class PersonalDetailsUpdateInput(
    @GraphQLDescription(PersonalDetailsDesc.NAME)
    val name: String? = null,
    @GraphQLDescription(PersonalDetailsDesc.PHONE)
    val phone: String? = null,
    @GraphQLDescription(PersonalDetailsDesc.DOB)
    val dob: String? = null,
    @GraphQLDescription(PersonalDetailsDesc.JOB)
    val job: String? = null,
    @GraphQLDescription(PersonalDetailsDesc.EDUCATION)
    val education: Education? = null,
    @GraphQLDescription(PersonalDetailsDesc.GENDER)
    val gender: Gender? = null,
    @GraphQLDescription(PersonalDetailsDesc.CURRENT_LOC)
    val currentLoc: Location? = null,
    @GraphQLDescription(PersonalDetailsDesc.LIVE_LOC)
    val liveLoc: SimpleLoc? = null,
    @GraphQLDescription(PersonalDetailsDesc.BIRTH_LOC)
    val birthLoc: SimpleLoc? = null,
)

enum class Gender {
    MALE, FEMALE, NON_BINARY, PREF_NOT_TO_SAY
}

object PersonalDetailsDesc {
    const val MODEL = "Represents a user's personal details."
    const val NAME = "Full name of the user."
    const val DOB = "Date of birth of the user. Format: YYYY-MM-DD Example: 1990-01-01"
    const val AGE = "Age of the user."
    const val PHONE = "Phone number of the user."
    const val JOB = "Job of the user."
    const val EDUCATION = "Education of the user."
    const val GENDER = "Gender of the user"
    const val CURRENT_LOC = "Current location of the user. Format: 'City, State, Country'"
    const val LIVE_LOC = "Location where the user lives. Format: 'City, State, Country'"
    const val BIRTH_LOC = "Birth location of the user. Format: 'City, State, Country'"
}
