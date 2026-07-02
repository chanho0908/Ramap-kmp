package com.peto.ramap.domain.model

import com.peto.ramap.fixture.ramenShopFixture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class RamenShopsTest {
    @Test
    fun `이미 렌더링된 가게는 제외한다`() {
        // given
        val renderedShopIds = mutableSetOf("1")
        val shops =
            RamenShops(
                mapOf(
                    "1" to ramenShopFixture(id = "1", name = "이미 렌더링된 가게"),
                    "2" to ramenShopFixture(id = "2", name = "새로운 가게"),
                ),
            )

        // when
        val result = shops.filterNotContainShops(renderedShopIds)

        // then
        assertEquals(listOf("2"), result.map { it.id })
    }

    @Test
    fun `필터가 비어있으면 모든 가게를 반환한다`() {
        // given
        val shops =
            RamenShops(
                mapOf(
                    "1" to
                        ramenShopFixture(
                            id = "1",
                            menuCategories = listOf(Category.MAZESOBA),
                        ),
                    "2" to
                        ramenShopFixture(
                            id = "2",
                            menuCategories = listOf(Category.JIRO),
                        ),
                ),
            )

        // when
        val result = shops.filterByCategory(RamenShopFilter())

        // then
        assertEquals(shops, result)
    }

    @Test
    fun `선택한 카테고리를 가진 가게만 반환한다`() {
        // given
        val shops =
            RamenShops(
                mapOf(
                    "1" to
                        ramenShopFixture(
                            id = "1",
                            menuCategories = listOf(Category.MAZESOBA),
                        ),
                    "2" to
                        ramenShopFixture(
                            id = "2",
                            menuCategories = listOf(Category.JIRO),
                        ),
                    "3" to
                        ramenShopFixture(
                            id = "3",
                            menuCategories = emptyList(),
                        ),
                ),
            )
        val filter = RamenShopFilter(setOf(Category.MAZESOBA))

        // when
        val result = shops.filterByCategory(filter)

        // then
        assertEquals(setOf("1"), result.keys)
    }

    @Test
    fun `가게가 여러 카테고리를 가지고 있으면 하나만 일치해도 반환한다`() {
        // given
        val shops =
            RamenShops(
                mapOf(
                    "1" to
                        ramenShopFixture(
                            id = "1",
                            menuCategories = listOf(Category.MAZESOBA, Category.JIRO),
                        ),
                    "2" to
                        ramenShopFixture(
                            id = "2",
                            menuCategories = emptyList(),
                        ),
                ),
            )
        val filter = RamenShopFilter(setOf(Category.JIRO))

        // when
        val result = shops.filterByCategory(filter)

        // then
        assertTrue("1" in result.keys)
        assertFalse("2" in result.keys)
    }
}
