package com.peto.ramap.ui.map

import com.peto.ramap.core.base.BaseViewModel
import com.peto.ramap.domain.model.MapBounds
import com.peto.ramap.domain.repository.RamenShopRepository
import com.peto.ramap.ui.map.contract.MapIntent
import com.peto.ramap.ui.map.contract.MapSideEffect
import com.peto.ramap.ui.map.contract.MapUiState

class MapViewModel(
    private val ramenShopRepository: RamenShopRepository,
) : BaseViewModel<MapUiState, MapIntent, MapSideEffect>(initialState = MapUiState()) {
    override suspend fun handleIntent(intent: MapIntent) {
        when (intent) {
            is MapIntent.OnBoundsChanged -> loadRamenShops(intent.bounds)
        }
    }

    private suspend fun loadRamenShops(bounds: MapBounds) {
        val result = ramenShopRepository.fetchRamenShops(bounds)
        reduce { copy(shops = result) }
    }
}
