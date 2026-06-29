package com.peto.ramap.ui.map.contract

import com.peto.ramap.core.base.State
import com.peto.ramap.domain.model.RamenShops

data class MapUiState(
    val shops: RamenShops = RamenShops(emptyList()),
) : State
