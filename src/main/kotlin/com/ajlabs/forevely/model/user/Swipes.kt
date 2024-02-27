package com.ajlabs.forevely.model.user

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import org.bson.types.ObjectId

@GraphQLDescription(SwipesDesc.MODEL)
data class Swipes(
    @GraphQLDescription(SwipesDesc.LIKES)
    val likes: List<ObjectId> = emptyList(),
    @GraphQLDescription(SwipesDesc.DISLIKES)
    val dislikes: List<ObjectId> = emptyList(),
)

@GraphQLDescription(SwipesDesc.SWIPE_TYPE)
enum class SwipeType {
    @GraphQLDescription(SwipesDesc.SWIPE_TYPE_LIKE)
    LIKE,

    @GraphQLDescription(SwipesDesc.SWIPE_TYPE_DISLIKE)
    DISLIKE,
}

object SwipesDesc {
    const val MODEL = "Model for swipes"
    const val LIKES = "List of ids of liked users"
    const val DISLIKES = "List of ids of disliked users"
    const val SWIPE_TYPE = "Type of swipe"
    const val SWIPE_TYPE_LIKE = "User liked other user"
    const val SWIPE_TYPE_DISLIKE = "User disliked other user"
}
