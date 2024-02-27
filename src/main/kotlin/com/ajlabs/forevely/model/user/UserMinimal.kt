package com.ajlabs.forevely.model.user

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import org.bson.types.ObjectId

@GraphQLDescription(UserMinimalDesc.MODEL)
data class UserMinimal(
    @GraphQLDescription(UserMinimalDesc.ID) val id: ObjectId,
    @GraphQLDescription(UserMinimalDesc.EMAIL) val email: String,
)

fun User.toMinimal() = UserMinimal(
    id = this.id,
    email = this.email,
)

object UserMinimalDesc {
    const val MODEL = "Represents a minimal user object"
    const val ID = UserDesc.ID
    const val EMAIL = UserDesc.EMAIL
}
