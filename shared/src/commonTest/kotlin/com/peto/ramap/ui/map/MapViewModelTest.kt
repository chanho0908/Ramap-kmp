package com.peto.ramap.ui.map

import app.cash.turbine.test
import com.peto.ramap.data.fixture.BOUNDS_FIXTURE
import com.peto.ramap.domain.model.Category
import com.peto.ramap.domain.model.Location
import com.peto.ramap.domain.model.MapBounds
import com.peto.ramap.domain.model.RamenShop
import com.peto.ramap.domain.model.RamenShops
import com.peto.ramap.domain.model.SearchQuery
import com.peto.ramap.domain.model.ShopWaitingSystem
import com.peto.ramap.domain.model.WaitingProvider
import com.peto.ramap.domain.repository.RamenShopRepository
import com.peto.ramap.domain.repository.ShopWaitingSystemRepository
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
                val ramenShopRepository = FakeRamenShopRepository()
                val viewModel = mapViewModel(ramenShopRepository)
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

                assertEquals(emptyList(), ramenShopRepository.requestedBoundsHistory)

                advanceTimeBy(1)
                runCurrent()

                assertEquals(listOf(lastBounds), ramenShopRepository.requestedBoundsHistory)
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test
    fun `마지막 성공 조회 영역과 의미 있게 다르지 않으면 조회하지 않는다`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val ramenShopRepository = FakeRamenShopRepository()
                val viewModel = mapViewModel(ramenShopRepository)
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

                assertEquals(listOf(BOUNDS_FIXTURE), ramenShopRepository.requestedBoundsHistory)
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test
    fun `마지막 성공 조회 영역과 의미 있게 다르면 새로 조회한다`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val ramenShopRepository = FakeRamenShopRepository()
                val viewModel = mapViewModel(ramenShopRepository)
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

                assertEquals(listOf(BOUNDS_FIXTURE, changedBounds), ramenShopRepository.requestedBoundsHistory)
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
                val ramenShopRepository = FakeRamenShopRepository(result = shops)
                val viewModel = mapViewModel(ramenShopRepository)
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

    @Test
    fun `가게를 선택하면 상세를 즉시 열고 웨이팅 시스템을 조회한다`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val shop = ramenShopFixture()
                val waitingSystem = waitingSystemFixture(shopId = shop.id)
                val waitingSystemRepository = FakeShopWaitingSystemRepository(result = waitingSystem)
                val viewModel = mapViewModel(shopWaitingSystemRepository = waitingSystemRepository)

                viewModel.dispatch(MapIntent.OnShopSelected(shop))
                runCurrent()

                assertEquals(shop, viewModel.uiState.value.selectedShop)
                assertEquals(listOf(shop.id), waitingSystemRepository.requestedShopIds)
                assertEquals(waitingSystem, viewModel.uiState.value.shopWaiting[shop.id])
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test
    fun `웨이팅 시스템 조회 결과가 없어도 선택한 가게 상세는 유지한다`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val shop = ramenShopFixture()
                val waitingSystemRepository = FakeShopWaitingSystemRepository(result = null)
                val viewModel = mapViewModel(shopWaitingSystemRepository = waitingSystemRepository)

                viewModel.dispatch(MapIntent.OnShopSelected(shop))
                runCurrent()

                val containsWaitingSystem =
                    viewModel
                        .uiState
                        .value
                        .shopWaiting
                        .containsKey(shop.id)

                assertEquals(shop, viewModel.uiState.value.selectedShop)
                assertEquals(listOf(shop.id), waitingSystemRepository.requestedShopIds)
                assertEquals(true, containsWaitingSystem)
                assertEquals(null, viewModel.uiState.value.shopWaiting[shop.id])
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test
    fun `이미 조회한 가게를 다시 선택하면 웨이팅 시스템을 중복 조회하지 않는다`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val shop = ramenShopFixture()
                val waitingSystemRepository =
                    FakeShopWaitingSystemRepository(result = waitingSystemFixture(shop.id))
                val viewModel = mapViewModel(shopWaitingSystemRepository = waitingSystemRepository)

                viewModel.dispatch(MapIntent.OnShopSelected(shop))
                runCurrent()
                viewModel.dispatch(MapIntent.OnShopSelected(shop))
                runCurrent()

                assertEquals(listOf(shop.id), waitingSystemRepository.requestedShopIds)
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test
    fun `선택된 가게가 있어도 지도 영역 변경만으로는 웨이팅 시스템을 조회하지 않는다`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val shop = ramenShopFixture()
                val ramenShopRepository = FakeRamenShopRepository()
                val waitingSystemRepository =
                    FakeShopWaitingSystemRepository(result = waitingSystemFixture(shop.id))
                val viewModel =
                    mapViewModel(
                        ramenShopRepository = ramenShopRepository,
                        shopWaitingSystemRepository = waitingSystemRepository,
                    )

                viewModel.dispatch(MapIntent.OnShopSelected(shop))
                runCurrent()
                waitingSystemRepository.requestedShopIds.clear()

                viewModel.dispatch(MapIntent.OnBoundsChanged(BOUNDS_FIXTURE))
                advanceTimeBy(350)
                runCurrent()

                assertEquals(listOf(BOUNDS_FIXTURE), ramenShopRepository.requestedBoundsHistory)
                assertEquals(emptyList(), waitingSystemRepository.requestedShopIds)
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test
    fun `검색어가 변경되면 지연 후 정규화한 검색어로 가게를 검색한다`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val shops = RamenShops(listOf(ramenShopFixture()).associateBy { it.id })
                val ramenShopRepository = FakeRamenShopRepository(searchResult = shops)
                val viewModel = mapViewModel(ramenShopRepository)

                viewModel.dispatch(MapIntent.OnQueryChanged("  RAMEN   SHOP  "))
                advanceTimeBy(299)
                runCurrent()

                assertEquals(emptyList(), ramenShopRepository.requestedSearchQueries)

                advanceTimeBy(1)
                runCurrent()

                assertEquals(listOf(SearchQuery("ramen shop")), ramenShopRepository.requestedSearchQueries)
                assertEquals(listOf(50), ramenShopRepository.requestedSearchLimits)
                assertEquals(shops, viewModel.uiState.value.searchResults)
                assertEquals(RamenShops(emptyMap()), viewModel.uiState.value.shops)
                assertEquals(shops, viewModel.uiState.value.markerShops)
                assertEquals(shops.value.values.toList(), viewModel.uiState.value.focusShops)
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test
    fun `검색 중에는 지도 영역 매장이 아닌 검색 결과만 마커로 보여준다`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val mapShops =
                    RamenShops(
                        listOf(
                            ramenShopFixture().copy(
                                id = "map-shop",
                                name = "지도 매장",
                            ),
                        ).associateBy { it.id },
                    )
                val searchShops =
                    RamenShops(
                        listOf(
                            ramenShopFixture().copy(
                                id = "search-shop",
                                name = "검색 매장",
                            ),
                        ).associateBy { it.id },
                    )
                val ramenShopRepository =
                    FakeRamenShopRepository(
                        result = mapShops,
                        searchResult = searchShops,
                    )
                val viewModel = mapViewModel(ramenShopRepository)

                viewModel.dispatch(MapIntent.OnBoundsChanged(BOUNDS_FIXTURE))
                advanceTimeBy(350)
                runCurrent()
                viewModel.dispatch(MapIntent.OnQueryChanged("라멘"))
                advanceTimeBy(300)
                runCurrent()

                assertEquals(mapShops, viewModel.uiState.value.shops)
                assertEquals(searchShops, viewModel.uiState.value.searchResults)
                assertEquals(searchShops, viewModel.uiState.value.markerShops)
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test
    fun `현재 검색 결과가 도착하기 전에는 기존 지도 영역 매장 마커를 유지한다`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val mapShops =
                    RamenShops(
                        listOf(
                            ramenShopFixture().copy(
                                id = "map-shop",
                                name = "지도 매장",
                            ),
                        ).associateBy { it.id },
                    )
                val searchShops =
                    RamenShops(
                        listOf(
                            ramenShopFixture().copy(
                                id = "search-shop",
                                name = "검색 매장",
                            ),
                        ).associateBy { it.id },
                    )
                val ramenShopRepository =
                    FakeRamenShopRepository(
                        result = mapShops,
                        searchResult = searchShops,
                    )
                val viewModel = mapViewModel(ramenShopRepository)

                viewModel.dispatch(MapIntent.OnBoundsChanged(BOUNDS_FIXTURE))
                advanceTimeBy(350)
                runCurrent()
                viewModel.dispatch(MapIntent.OnQueryChanged("라멘"))
                advanceTimeBy(299)
                runCurrent()

                assertEquals(mapShops, viewModel.uiState.value.markerShops)

                advanceTimeBy(1)
                runCurrent()

                assertEquals(searchShops, viewModel.uiState.value.markerShops)
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test
    fun `검색어가 연속으로 변경되면 마지막 검색어만 검색한다`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val ramenShopRepository = FakeRamenShopRepository()
                val viewModel = mapViewModel(ramenShopRepository)

                viewModel.dispatch(MapIntent.OnQueryChanged("라멘"))
                advanceTimeBy(150)
                viewModel.dispatch(MapIntent.OnQueryChanged("라멘집"))
                advanceTimeBy(299)
                runCurrent()

                assertEquals(emptyList(), ramenShopRepository.requestedSearchQueries)

                advanceTimeBy(1)
                runCurrent()

                assertEquals(listOf(SearchQuery("라멘집")), ramenShopRepository.requestedSearchQueries)
            } finally {
                Dispatchers.resetMain()
            }
        }

    @Test
    fun `검색어가 비어 있으면 검색 결과를 비우고 현재 지도 영역 매장을 보여준다`() =
        runTest {
            Dispatchers.setMain(StandardTestDispatcher(testScheduler))
            try {
                val mapShops =
                    RamenShops(
                        listOf(
                            ramenShopFixture().copy(
                                id = "map-shop",
                                name = "지도 매장",
                            ),
                        ).associateBy { it.id },
                    )
                val searchShops =
                    RamenShops(
                        listOf(
                            ramenShopFixture().copy(
                                id = "search-shop",
                                name = "검색 매장",
                            ),
                        ).associateBy { it.id },
                    )
                val ramenShopRepository =
                    FakeRamenShopRepository(
                        result = mapShops,
                        searchResult = searchShops,
                    )
                val viewModel = mapViewModel(ramenShopRepository)

                viewModel.dispatch(MapIntent.OnBoundsChanged(BOUNDS_FIXTURE))
                advanceTimeBy(350)
                runCurrent()
                viewModel.dispatch(MapIntent.OnQueryChanged("라멘"))
                advanceTimeBy(300)
                runCurrent()
                viewModel.dispatch(MapIntent.OnQueryChanged("   "))
                runCurrent()

                assertEquals(RamenShops(emptyMap()), viewModel.uiState.value.searchResults)
                assertEquals(mapShops, viewModel.uiState.value.shops)
                assertEquals(mapShops, viewModel.uiState.value.markerShops)
                assertEquals(listOf(SearchQuery("라멘")), ramenShopRepository.requestedSearchQueries)
            } finally {
                Dispatchers.resetMain()
            }
        }
}

private class FakeRamenShopRepository(
    private val result: RamenShops = RamenShops(emptyMap()),
    private val searchResult: RamenShops = RamenShops(emptyMap()),
) : RamenShopRepository {
    val requestedBoundsHistory = mutableListOf<MapBounds>()
    val requestedSearchQueries = mutableListOf<SearchQuery>()
    val requestedSearchLimits = mutableListOf<Int>()

    override suspend fun fetchRamenShops(bounds: MapBounds): RamenShops {
        requestedBoundsHistory += bounds
        return result
    }

    override suspend fun searchRamenShops(
        query: SearchQuery,
        limit: Int,
    ): RamenShops {
        requestedSearchQueries += query
        requestedSearchLimits += limit
        return searchResult
    }
}

private class FakeShopWaitingSystemRepository(
    private val result: ShopWaitingSystem? = null,
    private val error: Throwable? = null,
) : ShopWaitingSystemRepository {
    val requestedShopIds = mutableListOf<String>()

    override suspend fun fetchShopWaitingSystem(shopId: String): ShopWaitingSystem? {
        requestedShopIds += shopId
        error?.let { throw it }
        return result
    }
}

private fun mapViewModel(
    ramenShopRepository: RamenShopRepository = FakeRamenShopRepository(),
    shopWaitingSystemRepository: ShopWaitingSystemRepository = FakeShopWaitingSystemRepository(),
): MapViewModel = MapViewModel(ramenShopRepository, shopWaitingSystemRepository)

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
        menuCategories = listOf(Category.SHOYU),
        isVisible = true,
        createdAt = "2026-06-01T00:00:00Z",
        updatedAt = "2026-06-02T00:00:00Z",
    )

private fun waitingSystemFixture(shopId: String): ShopWaitingSystem =
    ShopWaitingSystem(
        id = "waiting-$shopId",
        shopId = shopId,
        provider = WaitingProvider.CATCHTABLE,
        providerUrl = "https://app.catchtable.co.kr/ct/shop/$shopId",
    )
