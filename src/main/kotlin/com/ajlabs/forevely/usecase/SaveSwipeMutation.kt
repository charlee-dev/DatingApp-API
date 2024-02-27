package com.ajlabs.forevely.usecase

import com.ajlabs.forevely.domain.util.OBJECT_ID
import com.ajlabs.forevely.domain.util.withCurrentUser
import com.ajlabs.forevely.model.user.SwipeType
import com.ajlabs.forevely.model.user.Swipes
import com.ajlabs.forevely.model.user.SwipesDesc
import com.ajlabs.forevely.model.user.User
import com.ajlabs.forevely.model.user.UserDesc
import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mongodb.kotlin.client.coroutine.MongoCollection
import com.mongodb.kotlin.client.coroutine.MongoDatabase
import graphql.schema.DataFetchingEnvironment
import org.bson.types.ObjectId
import org.koin.java.KoinJavaComponent

object SaveSwipeMutation : Mutation {
    private val database: MongoDatabase by KoinJavaComponent.inject(MongoDatabase::class.java)
    private val userCollection: MongoCollection<User> = database.getCollection<User>(User::class.java.simpleName)

    @GraphQLDescription("Mutation to user swipes")
    @Suppress("unused")
    suspend fun saveSwipe(
        dfe: DataFetchingEnvironment,
        @GraphQLDescription(SwipesDesc.SWIPE_TYPE)
        swipeType: SwipeType,
        @GraphQLDescription(UserDesc.ID)
        id: ObjectId,
    ): Boolean = dfe.withCurrentUser { executorId ->
        val filter = Filters.eq(OBJECT_ID, executorId)
        val fieldName = when (swipeType) {
            SwipeType.LIKE -> "${User::swipes.name}.${Swipes::likes.name}"
            SwipeType.DISLIKE -> "${User::swipes.name}.${Swipes::dislikes.name}"
        }
        val updates = Updates.addToSet(fieldName, id)
        userCollection.updateOne(filter, updates).wasAcknowledged()
    }
}
