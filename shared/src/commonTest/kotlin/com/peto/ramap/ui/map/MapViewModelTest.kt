package com.peto.ramap.ui.map

import app.cash.turbine.test
import com.peto.ramap.data.fixture.BOUNDS_FIXTURE
import com.peto.ramap.domain.model.Location
import com.peto.ramap.domain.model.MapBounds
import com.peto.ramap.domain.model.RamenShop
import com.peto.ramap.domain.model.RamenShops
import com.peto.ramap.domain.repository.RamenShopRepository
import com.peto.ramap.ui.map.contract.MapIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {
    @Test
    fun `연속 지도 영역 변경은 지연 후 마지막 영역만 조회한다`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repository = FakeRamenShopRepository()
                val viewModel = MapViewModel(repository)
                val lastBounds =
                    BOUNDS_FIXTURE.copy(
                        minLat = BOUNDS_FIXTURE.minLat + 0.03,
                        maxLat = BOUNDS_FIXTURE.maxLat + 0.03,
                    )

                viewModel.dispatch(MapIntent.OnBoundsChanged(BOUNDS_FIXTURE))
                advanceTimeBy(200)
                viewModel.dispatch(MapIntent.OnBoundsChanged(lastBounds))
                advanceTimeBy(349)
                runCurrent()

                assertEquals(emptyList(), repository.requestedBoundsHistory)

                advanceTimeBy(1)
                runCurrent()

                assertEquals(listOf(lastBounds), repository.requestedBoundsHistory)
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test
    fun `마지막 성공 조회 영역과 의미 있게 다르지 않으면 조회하지 않는다`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repository = FakeRamenShopRepository()
                val viewModel = MapViewModel(repository)
                val nearbyBounds =
                    BOUNDS_FIXTURE.copy(
                        minLat = BOUNDS_FIXTURE.minLat + 0.01,
                        maxLat = BOUNDS_FIXTURE.maxLat + 0.01,
                    )

                viewModel.dispatch(MapIntent.OnBoundsChanged(BOUNDS_FIXTURE))
                advanceTimeBy(350)
                runCurrent()
                viewModel.dispatch(MapIntent.OnBoundsChanged(nearbyBounds))
                advanceTimeBy(350)
                runCurrent()

                assertEquals(listOf(BOUNDS_FIXTURE), repository.requestedBoundsHistory)
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test
    fun `마지막 성공 조회 영역과 의미 있게 다르면 새로 조회한다`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val repository = FakeRamenShopRepository()
                val viewModel = MapViewModel(repository)
                val changedBounds =
                    BOUNDS_FIXTURE.copy(
                        minLat = BOUNDS_FIXTURE.minLat + 0.03,
                        maxLat = BOUNDS_FIXTURE.maxLat + 0.03,
                    )

                viewModel.dispatch(MapIntent.OnBoundsChanged(BOUNDS_FIXTURE))
                advanceTimeBy(350)
                runCurrent()
                viewModel.dispatch(MapIntent.OnBoundsChanged(changedBounds))
                advanceTimeBy(350)
                runCurrent()

                assertEquals(listOf(BOUNDS_FIXTURE, changedBounds), repository.requestedBoundsHistory)
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test
    fun `조회 결과가 현재 가게 목록과 같으면 UI 상태를 다시 방출하지 않는다`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val shops = RamenShops(listOf(ramenShopFixture()).associateBy { it.id })
                val repository = FakeRamenShopRepository(result = shops)
                val viewModel = MapViewModel(repository)
                val changedBounds =
                    BOUNDS_FIXTURE.copy(
                        minLat = BOUNDS_FIXTURE.minLat + 0.03,
                        maxLat = BOUNDS_FIXTURE.maxLat + 0.03,
                    )

                viewModel.uiState.test {
                    assertEquals(RamenShops(emptyMap()), awaitItem().shops)

                    viewModel.dispatch(MapIntent.OnBoundsChanged(BOUNDS_FIXTURE))
                    advanceTimeBy(350)
                    runCurrent()
                    assertEquals(shops, awaitItem().shops)

                    viewModel.dispatch(MapIntent.OnBoundsChanged(changedBounds))
                    advanceTimeBy(350)
                    runCurrent()
                    expectNoEvents()
                }
            } finally {
                Dispatchers.resetMain()
            }
        }
}

private class FakeRamenShopRepository(
    private val result: RamenShops = RamenShops(emptyMap()),
) : RamenShopRepository {
    val requestedBoundsHistory = mutableListOf<MapBounds>()

    override suspend fun fetchRamenShops(bounds: MapBounds): RamenShops {
        requestedBoundsHistory += bounds
        return result
    }
}

private fun ramenShopFixture(): RamenShop =
    RamenShop(
        id = "shop-1",
        kakaoPlaceId = "kakao-shop-1",
        name = "라멘집",
        address = "서울시 마포구 라멘로 1",
        location = Location(lat = 37.551, lng = 126.921),
        kakaoPlaceUrl = "https://place.map.kakao.com/shop-1",
        phone = "02-0000-0000",
        businessHours = "11:00-21:00",
        instagramUrl = "https://instagram.com/ramen_shop",
        kakaoRating = 4.5,
        menuCategoryIds = listOf("shoyu"),
        isVisible = true,
        createdAt = "2026-06-01T00:00:00Z",
        updatedAt = "2026-06-02T00:00:00Z",
    )
