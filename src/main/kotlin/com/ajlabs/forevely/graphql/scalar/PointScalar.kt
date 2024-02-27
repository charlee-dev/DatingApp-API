package com.ajlabs.forevely.graphql.scalar

import com.mongodb.client.model.geojson.Point
import com.mongodb.client.model.geojson.Position
import graphql.GraphQLContext
import graphql.execution.CoercedVariables
import graphql.language.ObjectValue
import graphql.language.Value
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import java.util.Locale

val graphqlPointType: GraphQLScalarType = GraphQLScalarType.newScalar()
    .name("Point")
    .description("A type representing a MongoDB GeoJSON Point")
    .coercing(PointCoercing)
    .build()

private object PointCoercing : Coercing<Point, Map<String, Any>> {
    override fun serialize(
        dataFetcherResult: Any,
        graphQLContext: GraphQLContext,
        locale: Locale,
    ): Map<String, Any> {
        if (dataFetcherResult is Point) {
            return mapOf(
                "coordinates" to listOf(
                    dataFetcherResult.coordinates.values[0],
                    dataFetcherResult.coordinates.values[1],
                ),
            )
        }
        throw CoercingSerializeException("Data fetcher result $dataFetcherResult cannot be serialized to a Point")
    }

    override fun parseValue(
        input: Any,
        graphQLContext: GraphQLContext,
        locale: Locale,
    ): Point {
        if (input is Map<*, *>) {
            val coordinates = input["coordinates"] as? List<*>

            if (coordinates is List<*> && coordinates.size == 2) {
                val longitude = coordinates[0].toString().toDouble()
                val latitude = coordinates[1].toString().toDouble()
                val position = Position(longitude, latitude)
                return Point(position)
            }
        }
        throw CoercingParseValueException("Expected valid Point format but was $input")
    }

    override fun parseLiteral(
        input: Value<*>,
        variables: CoercedVariables,
        graphQLContext: GraphQLContext,
        locale: Locale,
    ): Point {
        if (input is ObjectValue) {
            val map = input.objectFields.associate { it.name to it.value }
            return parseValue(map, graphQLContext, locale)
        }
        throw CoercingParseLiteralException("Invalid input type for Point: $input")
    }
}
