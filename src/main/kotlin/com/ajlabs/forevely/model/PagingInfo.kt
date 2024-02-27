package com.ajlabs.forevely.model

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import graphql.GraphQLException

private const val DEFAULT_PAGE = 0
private const val DEFAULT_SIZE = 10
private const val MIN_PAGE = 0
private const val MAX_SIZE = 100
private const val MIN_SIZE = 1

@GraphQLDescription(PagingInfoDesc.MODEL)
data class PagingInfo(
    @GraphQLDescription(PagingInfoDesc.COUNT)
    var count: Int,
    @GraphQLDescription(PagingInfoDesc.PAGES)
    var pages: Int,
    @GraphQLDescription(PagingInfoDesc.NEXT)
    var next: Int?,
    @GraphQLDescription(PagingInfoDesc.PREV)
    var prev: Int?,
)

@GraphQLDescription(PageInputDesc.MODEL)
data class PageInput(
    @GraphQLDescription(PageInputDesc.page)
    val page: Int = DEFAULT_PAGE,
    @GraphQLDescription(PageInputDesc.size)
    val size: Int = 10,
) {
    fun validate() {
        require(page >= MIN_PAGE) { throw GraphQLException("Page must be >= $MIN_PAGE") }
        require(size in MIN_SIZE..MAX_SIZE) { throw GraphQLException("Size must be between $MIN_SIZE and $MAX_SIZE") }
    }
}

data class Page<T>(
    val results: List<T>,
    @GraphQLDescription(PagingInfoDesc.MODEL)
    val info: PagingInfo,
)

object PagingInfoDesc {
    const val MODEL = "Metadata about the pagination"
    const val COUNT = "The total number of items across all pages."
    const val PAGES = "The total number of pages."
    const val NEXT = "The next page number, or null if this is the last page."
    const val PREV = "The previous page number, or null if this is the first page (page $MIN_PAGE)."
}

object PageInputDesc {
    const val MODEL = "The name of the data model being paginated."
    const val page = "The current page number to get. Negative page number is invalid. Default is $DEFAULT_PAGE."
    const val size =
        "The number of items to return per page. Must be between $MIN_SIZE and $MAX_SIZE. Default is $DEFAULT_SIZE."
}
