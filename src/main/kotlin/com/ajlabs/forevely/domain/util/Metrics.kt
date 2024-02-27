package com.ajlabs.forevely.domain.util

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

fun kmToLatitudeDegrees(km: Double): Double {
    val earthCircumferenceKm = 40075.0 // Earth's circumference along the equator in kilometers
    val degreesInCircle = 360.0 // Total degrees in a circle

    // Convert Earth's circumference to meters and calculate the distance per degree
    val distancePerDegree = (earthCircumferenceKm * 1000) / degreesInCircle

    // Convert the input distance from kilometers to meters and calculate the corresponding latitude degrees
    val latitudeDegrees = (km * 1000) / distancePerDegree
    return latitudeDegrees
}

private const val ONE_RADIANT = 6371.0

fun kmToRadians(km: Int): Double = km / ONE_RADIANT

fun kmToRadians(km: Double): Double = km / ONE_RADIANT

fun kmToMiles(km: Int): Double = km * ONE_RADIANT

fun kmToMiles(km: Double): Double = km * ONE_RADIANT

@Suppress("MagicNumber")
private fun Double.toRadians(): Double = PI * this / 180.0

/**
 * Calculates the great-circle distance between two points on a sphere given their
 * longitudes and latitudes.
 */
@Suppress("MagicNumber")
fun haversine(
    lat1: Double,
    lng1: Double,
    lat2: Double,
    lbg2: Double,
    earthRadius: Double = 6372.8,
): Double {
    val lat1r = lat1.toRadians()
    val lat2r = (lat2).toRadians()
    val deltaLat = (lat2 - lat1).toRadians()
    val deltaLon = (lbg2 - lng1).toRadians()
    return 2 * earthRadius * asin(
        sqrt(
            sin(deltaLat / 2).pow(2) + sin(deltaLon / 2).pow(2) * cos(
                lat1r,
            ) * cos(lat2r),
        ),
    )
}
