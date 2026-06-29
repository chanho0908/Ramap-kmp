package com.peto.ramap.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.peto.ramap.domain.model.MapBounds
import com.peto.ramap.domain.model.RamenShops

@Composable
expect fun KakaoMapView(
    shops: RamenShops,
    onBoundsChanged: (MapBounds) -> Unit,
    modifier: Modifier = Modifier,
)
