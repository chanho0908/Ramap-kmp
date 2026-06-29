package com.peto.ramap.ui.map

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.peto.ramap.domain.model.MapBounds
import com.peto.ramap.ui.map.contract.MapIntent
import com.peto.ramap.ui.map.contract.MapUiState
import org.koin.compose.koinInject

@Composable
fun MapRoute(viewModel: MapViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MapScreen(
        uiState = uiState,
        onBoundsChanged = { bounds ->
            viewModel.dispatch(MapIntent.OnBoundsChanged(bounds))
        },
    )
}

@Composable
private fun MapScreen(
    uiState: MapUiState,
    onBoundsChanged: (MapBounds) -> Unit,
) {
    KakaoMapView(
        modifier = Modifier.fillMaxSize(),
        shops = uiState.shops,
        onBoundsChanged = onBoundsChanged,
    )
}
