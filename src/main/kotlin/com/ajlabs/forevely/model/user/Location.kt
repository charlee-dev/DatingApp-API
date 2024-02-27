package com.ajlabs.forevely.model.user

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.mongodb.client.model.geojson.Point

@GraphQLDescription(LocationDesc.MODEL)
data class Location(
    @GraphQLDescription(LocationDesc.POINT)
    val geo: Point? = null,
    @GraphQLDescription(LocationDesc.CITY)
    val city: String? = null,
    @GraphQLDescription(LocationDesc.STATE)
    val state: String? = null,
    @GraphQLDescription(LocationDesc.COUNTRY)
    val country: String? = null,
)

@GraphQLDescription(LocationDesc.MODEL)
data class CurrentLoc(
    @GraphQLDescription(LocationDesc.LONGITUDE)
    val longitude: Double? = null,
    @GraphQLDescription(LocationDesc.LATITUDE)
    val latitude: Double? = null,
    @GraphQLDescription(LocationDesc.CITY)
    val city: String? = null,
    @GraphQLDescription(LocationDesc.STATE)
    val state: String? = null,
    @GraphQLDescription(LocationDesc.COUNTRY)
    val country: String? = null,
    @GraphQLDescription(LocationDesc.DISTANCE)
    val distance: Int? = null,
)

data class SimpleLoc(
    @GraphQLDescription(LocationDesc.COUNTRY)
    val country: String?,
    @GraphQLDescription(LocationDesc.CITY)
    val city: String?,
)

object LocationDesc {
    const val MODEL = "Represents a user's location."
    const val POINT = "Point is mongoDb type of the user's location. " +
        "[See](https://www.mongodb.com/docs/manual/geospatial-queries/#legacy-coordinate-pairs)" +
        "Pass it as: Point(coordinates=[12.123, 45.678])"
    const val LATITUDE = "Latitude of the user's location."
    const val LONGITUDE = "Longitude of the user's location."
    const val CITY = "City of the user."
    const val STATE = "State of the user."
    const val COUNTRY = "Country of the user."
    const val DISTANCE = "Distance of the user from the current user"
}
