package com.peto.ramap.ui.map.model

import com.peto.ramap.domain.model.RamenShop

sealed interface RamenShopSelectState {
    data object UnSelected : RamenShopSelectState

    data class Selected(
        val value: RamenShop,
    ) : RamenShopSelectState
}
