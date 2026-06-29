package com.peto.ramap.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MapBoundsTest {
    @Test
    fun `지도 영역의 중심과 span을 계산한다`() {
        val bounds =
            MapBounds(
                minLat = 37.50,
                maxLat = 37.60,
                minLng = 126.90,
                maxLng = 127.10,
            )

        assertEquals(37.55, bounds.centerLat, DOUBLE_TOLERANCE)
        assertEquals(127.00, bounds.centerLng, DOUBLE_TOLERANCE)
        assertEquals(0.10, bounds.latSpan, DOUBLE_TOLERANCE)
        assertEquals(0.20, bounds.lngSpan, DOUBLE_TOLERANCE)
    }

    @Test
    fun `중심 이동이 기준보다 작으면 의미 있는 변경으로 판단하지 않는다`() {
        val previous =
            MapBounds(
                minLat = 37.50,
                maxLat = 37.60,
                minLng = 126.90,
                maxLng = 127.00,
            )
        val current =
            previous.copy(
                minLat = previous.minLat + 0.01,
                maxLat = previous.maxLat + 0.01,
            )

        assertFalse(current.isMeaningfullyDifferentFrom(previous))
    }

    @Test
    fun `중심 이동이 기준 이상이면 의미 있는 변경으로 판단한다`() {
        val previous =
            MapBounds(
                minLat = 37.50,
                maxLat = 37.60,
                minLng = 126.90,
                maxLng = 127.00,
            )
        val current =
            previous.copy(
                minLat = previous.minLat + 0.02,
                maxLat = previous.maxLat + 0.02,
            )

        assertTrue(current.isMeaningfullyDifferentFrom(previous))
    }

    @Test
    fun `span 변화가 기준 이상이면 의미 있는 변경으로 판단한다`() {
        val previous =
            MapBounds(
                minLat = 37.50,
                maxLat = 37.60,
                minLng = 126.90,
                maxLng = 127.00,
            )
        val current =
            previous.copy(
                maxLat = previous.maxLat + 0.015,
            )

        assertTrue(current.isMeaningfullyDifferentFrom(previous))
    }

    companion object {
        private const val DOUBLE_TOLERANCE = 0.000001
    }
}
