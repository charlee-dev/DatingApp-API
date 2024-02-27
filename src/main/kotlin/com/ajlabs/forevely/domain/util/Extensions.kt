package com.ajlabs.forevely.domain.util

import at.favre.lib.crypto.bcrypt.BCrypt
import com.ajlabs.forevely.model.Page
import com.ajlabs.forevely.model.PageInput
import com.ajlabs.forevely.model.PagingInfo
import com.mongodb.kotlin.client.coroutine.FindFlow
import com.mongodb.kotlin.client.coroutine.MongoCollection
import graphql.GraphQLException
import graphql.schema.DataFetchingEnvironment
import kotlinx.coroutines.flow.toList
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import java.nio.charset.StandardCharsets
import java.security.SecureRandom

const val OBJECT_ID = "_id"

suspend fun <T> DataFetchingEnvironment.withCurrentUser(block: suspend (userId: ObjectId) -> T): T {
    val id = graphQlContext.get<String>("id") ?: throw GraphQLException(ErrorMessage.NOT_SIGNED_IN)
    val objectId = ObjectId(id)
    return block(objectId)
}

private const val HASH_COST = 10
fun String.encrypt(salt: ByteArray): ByteArray = BCrypt.withDefaults()
    .hash(HASH_COST, salt, this.toByteArray(StandardCharsets.UTF_8))

fun generateSalt(): ByteArray {
    val random = SecureRandom()
    val salt = ByteArray(16)
    random.nextBytes(salt)
    return salt
}

suspend fun <T : Any> MongoCollection<T>.asPage(
    filter: Bson,
    pageInput: PageInput,
    findFlow: suspend MongoCollection<T>.(Bson) -> FindFlow<T>,
): Page<T> {
    val skips = (pageInput.page - 1) * pageInput.size
    val documents = findFlow(filter)
        .skip(skips)
        .limit(pageInput.size)
        .partial(true)
        .toList()

    val total = this.countDocuments(filter).toInt()
    val pagingInfo = getInfo(total, pageInput)
    return Page(documents, pagingInfo)
}

fun getInfo(total: Int, pageInput: PageInput): PagingInfo {
    val totalPages = (total + pageInput.size - 1) / pageInput.size
    val next = if (pageInput.page < totalPages - 1) pageInput.page + 1 else null
    val prev = if (pageInput.page > 0) pageInput.page - 1 else null

    return PagingInfo(
        count = total,
        pages = totalPages,
        next = next,
        prev = prev,
    )
}
