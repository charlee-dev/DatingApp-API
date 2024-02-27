package com.ajlabs.forevely.graphql.scalar

import graphql.GraphQLContext
import graphql.execution.CoercedVariables
import graphql.language.ObjectValue
import graphql.language.StringValue
import graphql.language.Value
import graphql.schema.Coercing
import graphql.schema.CoercingParseLiteralException
import graphql.schema.CoercingParseValueException
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import org.bson.types.ObjectId
import java.util.Locale

val graphqlObjectIdType: GraphQLScalarType = GraphQLScalarType.newScalar()
    .name("ObjectId")
    .description("A type representing a MongoDB ObjectID")
    .coercing(ObjectIdCoercing)
    .build()

private object ObjectIdCoercing : Coercing<ObjectId, String> {
    private const val HEX_LENGTH = 24
    private const val TS_POD_START_LENGTH = 8
    private const val DATE_POD_START_LENGTH = 18

    override fun parseValue(
        input: Any,
        graphQLContext: GraphQLContext,
        locale: Locale,
    ): ObjectId {
        return when (input) {
            is Map<*, *> -> fromMap(input)
            else -> runCatching {
                ObjectId(serialize(input, graphQLContext, locale))
            }.getOrElse {
                throw CoercingParseValueException("Expected valid ObjectId format but was $input")
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun fromMap(map: Map<*, *>): ObjectId {
        val timestamp = map["timestamp"] as? Int
        val date = map["date"] as? Long

        if (timestamp != null && date != null) {
            val timestampHexString =
                timestamp.toHexString().padStart(TS_POD_START_LENGTH, '0') // ensures 8 characters
            val dateHexString = date.toHexString().padStart(DATE_POD_START_LENGTH, '0') // ensures 16 characters

            val combinedString = "$timestampHexString$dateHexString"
            if (combinedString.length != HEX_LENGTH) {
                throw CoercingParseValueException("Failed to construct a valid ObjectId from the provided values")
            }

            return ObjectId(combinedString)
        } else {
            throw CoercingParseValueException("Invalid map format for ObjectId: $map")
        }
    }

    override fun parseLiteral(
        input: Value<*>,
        variables: CoercedVariables,
        graphQLContext: GraphQLContext,
        locale: Locale,
    ): ObjectId {
        when (input) {
            is ObjectValue -> {
                val map = input.objectFields.associate { it.name to it.value }
                return fromMap(map)
            }

            is StringValue -> {
                return runCatching {
                    ObjectId(input.value)
                }.getOrElse {
                    throw CoercingParseLiteralException("Expected valid ObjectId literal but was ${input.value}")
                }
            }

            else -> {
                throw CoercingParseLiteralException("Invalid input type for ObjectId: $input")
            }
        }
    }

    override fun serialize(
        dataFetcherResult: Any,
        graphQLContext: GraphQLContext,
        locale: Locale,
    ): String =
        runCatching {
            dataFetcherResult.toString()
        }.getOrElse {
            throw CoercingSerializeException("Data fetcher result $dataFetcherResult cannot be serialized to a String")
        }
}
