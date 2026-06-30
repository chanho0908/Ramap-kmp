package com.peto.ramap.ui.map

import com.peto.ramap.core.base.BaseViewModel
import com.peto.ramap.domain.model.MapBounds
import com.peto.ramap.domain.model.RamenShops
import com.peto.ramap.domain.repository.RamenShopRepository
import com.peto.ramap.ui.map.contract.MapIntent
import com.peto.ramap.ui.map.contract.MapSideEffect
import com.peto.ramap.ui.map.contract.MapUiState
import com.peto.ramap.ui.map.model.RamenShopSelectState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

class MapViewModel(
    private val ramenShopRepository: RamenShopRepository,
) : BaseViewModel<MapUiState, MapIntent, MapSideEffect>(initialState = MapUiState()) {
    private var boundsLoadJob: Job? = null
    private var boundsLoadRequestId = 0L
    private var lastLoadedBounds: MapBounds? = null

    override suspend fun handleIntent(intent: MapIntent) {
        when (intent) {
            is MapIntent.OnBoundsChanged -> scheduleRamenShopsLoad(intent.bounds)
            is MapIntent.OnShopSelected -> {
                reduce { copy(selectedShop = RamenShopSelectState.Selected(intent.shop)) }
            }

            is MapIntent.OnShopDetailDismissed -> {
                reduce { copy(selectedShop = RamenShopSelectState.UnSelected) }
            }
        }
    }

    /**
     * 지도 이동 이벤트를 바로 API 호출로 연결하지 않고 짧게 지연한다.
     *
     * 사용자가 지도를 연속해서 움직이면 이전 작업을 취소하고 마지막 bounds만 남겨,
     * 드래그 중간 지점마다 라멘 가게 목록을 다시 조회하지 않도록 한다.
     */
    private fun scheduleRamenShopsLoad(bounds: MapBounds) {
        boundsLoadJob?.cancel()
        val requestId = ++boundsLoadRequestId
        boundsLoadJob =
            runTask {
                delay(BOUNDS_LOAD_DEBOUNCE_MILLIS.milliseconds)
                loadRamenShops(bounds, requestId)
            }
    }

    /**
     * 마지막 성공 조회 영역과 비교해 충분히 달라진 경우에만 목록을 조회한다.
     *
     * 요청 취소를 협조하지 못한 오래된 작업이 늦게 끝나더라도 최신 request id와 다르면
     * 결과를 버리고, 조회 결과가 기존 UI 상태와 같으면 state 갱신도 생략해 마커 재렌더링을 줄인다.
     */
    private suspend fun loadRamenShops(
        bounds: MapBounds,
        requestId: Long,
    ) {
        val previousBounds = lastLoadedBounds
        if (previousBounds != null && !bounds.isMeaningfullyDifferentFrom(previousBounds)) return

        val result = ramenShopRepository.fetchRamenShops(bounds)
        if (requestId != boundsLoadRequestId) return

        lastLoadedBounds = bounds

        val mergedShops =
            RamenShops(
                value = currentState.shops.value + result.value,
            )

        if (currentState.shops != mergedShops) {
            reduce { copy(shops = mergedShops) }
        }
    }

    companion object {
        private const val BOUNDS_LOAD_DEBOUNCE_MILLIS = 350L
    }
}
