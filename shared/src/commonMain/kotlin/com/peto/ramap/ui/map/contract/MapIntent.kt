package com.peto.ramap.ui.map.contract

import com.peto.ramap.core.base.Intent
import com.peto.ramap.domain.model.MapBounds
import com.peto.ramap.domain.model.RamenShop

sealed interface MapIntent : Intent {
    data class OnBoundsChanged(
        val bounds: MapBounds,
    ) : MapIntent

    data class OnShopSelected(
        val shop: RamenShop,
    ) : MapIntent

    data object OnShopDetailDismissed : MapIntent

    data object OnSearchResultsDismissed : MapIntent

    data class OnQueryChanged(
        val query: String,
    ) : MapIntent
}
