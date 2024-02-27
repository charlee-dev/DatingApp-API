package com.ajlabs.forevely.domain

import com.ajlabs.forevely.domain.util.getInfo
import com.ajlabs.forevely.model.PageInput
import com.ajlabs.forevely.model.PagingInfo
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PagingInfoTests {
    @Test
    fun `test empty list returns no pages`() {
        val total = 0
        val items = listOf<Int>()
        val pageInput = PageInput(0, 10)

        val actual = getInfo(total, pageInput)
        val expected = PagingInfo(0, 0, null, null)

        assertEquals(expected, actual)
    }

    @Test
    fun `when 1 item should return correct pageInfo`() {
        val total = 1
        val items = listOf(1)
        val pageInput = PageInput(0, 10)

        val actual = getInfo(total, pageInput)
        val expected = PagingInfo(
            count = 1,
            pages = 1,
            next = null,
            prev = null,
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `test single page full items`() {
        val total = 10
        val items = (1..10).toList()
        val pageInput = PageInput(0, 10)

        val actual = getInfo(total, pageInput)
        val expected = PagingInfo(10, 1, null, null)

        assertEquals(expected, actual)
    }

    @Test
    fun `test multiple pages last page partially filled`() {
        val total = 15
        val items = (1..5).toList() // Assuming this is the third page
        val pageInput = PageInput(2, 10)

        val actual = getInfo(total, pageInput)
        val expected = PagingInfo(15, 2, null, 1)

        assertEquals(expected, actual)
    }

    @Test
    fun `test boundary condition at page size`() {
        val total = 10
        val items = (1..10).toList()
        val pageInput = PageInput(0, 10)

        val actual = getInfo(total, pageInput)
        val expected = PagingInfo(10, 1, null, null)

        assertEquals(expected, actual)
    }

    @Test
    fun `test null next on last page`() {
        val total = 20
        val items = (11..20).toList()
        val pageInput = PageInput(1, 10)

        val actual = getInfo(total, pageInput)
        val expected = PagingInfo(20, 2, null, 0)

        assertEquals(expected, actual)
    }

    @Test
    fun `test null prev on first page`() {
        val total = 20
        val items = (1..10).toList()
        val pageInput = PageInput(0, 10)

        val actual = getInfo(total, pageInput)
        val expected = PagingInfo(20, 2, 1, null)

        assertEquals(expected, actual)
    }
}
