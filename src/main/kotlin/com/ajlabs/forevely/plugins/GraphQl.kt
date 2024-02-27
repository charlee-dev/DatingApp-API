package com.ajlabs.forevely.plugins

import com.ajlabs.forevely.graphql.CustomGraphQLContextFactory
import com.ajlabs.forevely.graphql.CustomSchemaGeneratorHooks
import com.ajlabs.forevely.usecase.CreateConversationMutation
import com.ajlabs.forevely.usecase.ForgotPasswordQuery
import com.ajlabs.forevely.usecase.GetConversationsPageQuery
import com.ajlabs.forevely.usecase.GetMatchedLikesWithoutConversationQuery
import com.ajlabs.forevely.usecase.GetMatchersPageQuery
import com.ajlabs.forevely.usecase.GetMessagesPageQuery
import com.ajlabs.forevely.usecase.GetUserByIdQuery
import com.ajlabs.forevely.usecase.GetUserQuery
import com.ajlabs.forevely.usecase.LoginMutation
import com.ajlabs.forevely.usecase.RegisterMutation
import com.ajlabs.forevely.usecase.SaveSwipeMutation
import com.ajlabs.forevely.usecase.SendMessageMutation
import com.ajlabs.forevely.usecase.UpdateUserMutation
import com.ajlabs.forevely.usecase.debug.DebugSchema
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.generator.federation.directives.ContactDirective
import com.expediagroup.graphql.server.Schema
import com.expediagroup.graphql.server.ktor.GraphQL
import com.expediagroup.graphql.server.ktor.KtorGraphQLRequestParser
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.server.application.Application
import io.ktor.server.application.install

/**
 *  Docs: [graphql-kotlin](https://opensource.expediagroup.com/graphql-kotlin/docs/server/ktor-server/ktor-overview)
 */

private const val PACKAGE_NAME = "com.ajlabs.forevely"

fun Application.configureGraphQL() {
    install(GraphQL) {
        schema {
            packages = listOf(
                "$PACKAGE_NAME.model",
                "$PACKAGE_NAME.usecase",
            )
            queries = listOf(
                DebugSchema.Queries(),
                ForgotPasswordQuery,
                GetUserQuery,
                GetUserByIdQuery,
                GetMatchersPageQuery,
                GetMatchedLikesWithoutConversationQuery,
                GetConversationsPageQuery,
                GetMessagesPageQuery,
            )

            mutations = listOf(
                DebugSchema.Mutations(),
                LoginMutation,
                RegisterMutation,
                UpdateUserMutation,
                SaveSwipeMutation,
                CreateConversationMutation,
                SendMessageMutation,
            )

            subscriptions = listOf()

            schemaObject = ForevelySchema()
            hooks = CustomSchemaGeneratorHooks()
        }
        server {
            contextFactory = CustomGraphQLContextFactory()
            jacksonConfiguration = {
                enable(SerializationFeature.INDENT_OUTPUT)
                setDefaultLeniency(true)
                registerModules(
                    KotlinModule(
                        nullToEmptyMap = true,
                        nullToEmptyCollection = true,
                    ),
                )
            }
            requestParser = KtorGraphQLRequestParser(jacksonObjectMapper().apply(jacksonConfiguration))
        }
    }
}

@ContactDirective(
    name = "CharLEE-X",
    url = "https://github.com/CharLEE-X",
    description = "Send urgent issues to [#discussions](https://github.com/orgs/CharLEE-X/discussions).",
)
@GraphQLDescription("My schema description")
class ForevelySchema : Schema
