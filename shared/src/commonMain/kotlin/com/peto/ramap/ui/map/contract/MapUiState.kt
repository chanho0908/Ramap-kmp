package com.peto.ramap.ui.map.contract

import com.peto.ramap.core.base.State
import com.peto.ramap.domain.model.RamenShops
import com.peto.ramap.ui.map.model.RamenShopSelectState

data class MapUiState(
    val shops: RamenShops = RamenShops(emptyMap()),
    val selectedShop: RamenShopSelectState = RamenShopSelectState.UnSelected,
) : State
