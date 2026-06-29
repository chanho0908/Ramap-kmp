package com.peto.ramap.ui.map.contract

import com.peto.ramap.core.base.Intent
import com.peto.ramap.domain.model.MapBounds

sealed interface MapIntent : Intent {
    data class OnBoundsChanged(
        val bounds: MapBounds,
    ) : MapIntent
}
