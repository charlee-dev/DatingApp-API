package com.ajlabs.forevely.usecase

import com.ajlabs.forevely.domain.util.OBJECT_ID
import com.ajlabs.forevely.domain.util.getInfo
import com.ajlabs.forevely.domain.util.withCurrentUser
import com.ajlabs.forevely.model.Page
import com.ajlabs.forevely.model.PageInput
import com.ajlabs.forevely.model.PageInputDesc
import com.ajlabs.forevely.model.PagingInfo
import com.ajlabs.forevely.model.user.PersonalDetails
import com.ajlabs.forevely.model.user.PersonalDetailsDesc
import com.ajlabs.forevely.model.user.Profile
import com.ajlabs.forevely.model.user.User
import com.ajlabs.forevely.model.user.UserDesc
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Projections
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import org.koin.java.KoinJavaComponent

object GetMatchedLikesWithoutConversationQuery : Query {
    private val database by KoinJavaComponent.inject<MongoDatabase>(MongoDatabase::class.java)
    private val usersCollection by lazy {
        database.getCollection<User>(User::class.java.simpleName)
    }

    data class UserSwipes(val likes: List<ObjectId>)
    data class UserSwipesAndConversations(val swipes: UserSwipes, val conversationIds: List<ObjectId>)

    data class UserDetails(val name: String)
    data class UserProfile(val pictures: List<String>)
    data class MatcherMinimal(
        @BsonId
        val id: ObjectId,
        val details: UserDetails,
        val profile: UserProfile,
    )

    @Suppress("unused")
    @GraphQLDescription("Get matched users without conversation")
    suspend fun getMatchedUsersWithoutConversation(
        dfe: DataFetchingEnvironment,
        @GraphQLDescription(PageInputDesc.MODEL)
        pageInput: PageInput,
    ): ConversationMatchersPage = dfe.withCurrentUser { currentUserId ->
        // Getting swipes and conversations of the current user
        val currentUserSwipesAndConversations = getUserSwipesAndConversations(currentUserId)

        // Storing current user likes and conversations
        val currentUserLikes = currentUserSwipesAndConversations.swipes.likes
        val currentUserConversations = currentUserSwipesAndConversations.conversationIds

        // Getting liked users without conversation with current user
        val page =
            getLikedUsersWithoutConversation(currentUserLikes, currentUserConversations, pageInput)
        val matchers = page.results.map { it.toMatcherWithoutConversation() }

        ConversationMatchersPage(
            matchers = matchers,
            info = page.info,
        )
    }

    private suspend fun getUserSwipesAndConversations(userId: ObjectId): UserSwipesAndConversations {
        val filters = Filters.eq(OBJECT_ID, userId)
        val projection = Projections.fields(
            Projections.excludeId(),
            Projections.include(
                "${UserSwipesAndConversations::swipes.name}.${UserSwipes::likes.name}",
                UserSwipesAndConversations::conversationIds.name,
            ),
        )
        return usersCollection.find<UserSwipesAndConversations>(filters).projection(projection).firstOrNull()
            ?: error("User $userId not found")
    }

    private suspend fun getLikedUsersWithoutConversation(
        userLikes: List<ObjectId>,
        userConversations: List<ObjectId>,
        pageInput: PageInput,
    ): Page<MatcherMinimal> {
        val matcherFilter = Filters.and(
            Filters.`in`(OBJECT_ID, userLikes),
            Filters.nin(User::conversationIds.name, userConversations),
        )
        val matcherProjection = Projections.fields(
            Projections.include(
                "${User::details.name}.${PersonalDetails::name.name}",
                "${User::profile.name}.${Profile::pictures.name}",
            ),
        )
        val skips = (pageInput.page - 1) * pageInput.size

        val matchers = usersCollection.find<MatcherMinimal>(matcherFilter)
            .projection(matcherProjection)
            .skip(skips)
            .limit(pageInput.size)
            .partial(true)
            .toList()

        val total = usersCollection.countDocuments(matcherFilter).toInt()
        val pagingInfo = getInfo(total, pageInput)

        return Page(
            results = matchers,
            info = pagingInfo,
        )
    }

    private fun MatcherMinimal.toMatcherWithoutConversation() = MatcherWithoutConversation(
        id = this.id,
        name = this.details.name,
        picture = this.profile.pictures.firstOrNull(),
    )
}

data class ConversationMatchersPage(
    val matchers: List<MatcherWithoutConversation>,
    val info: PagingInfo,
)

data class MatcherWithoutConversation(
    @BsonId
    @GraphQLDescription(UserDesc.ID)
    val id: ObjectId,
    @GraphQLDescription(PersonalDetailsDesc.NAME)
    val name: String,
    @GraphQLDescription(UserDesc.EMAIL)
    val picture: String?,
)
