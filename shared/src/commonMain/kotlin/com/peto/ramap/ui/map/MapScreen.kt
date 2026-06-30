package com.peto.ramap.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.peto.ramap.designsystem.bottomsheet.CommonBottomSheet
import com.peto.ramap.designsystem.bottomsheet.CommonBottomSheetConfig
import com.peto.ramap.domain.model.MapBounds
import com.peto.ramap.domain.model.RamenShop
import com.peto.ramap.ui.map.component.RamenShopDetailContent
import com.peto.ramap.ui.map.contract.MapIntent
import com.peto.ramap.ui.map.contract.MapUiState
import com.peto.ramap.ui.map.model.RamenShopSelectState
import org.koin.compose.koinInject

@Composable
fun MapRoute(viewModel: MapViewModel = koinInject()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MapScreen(
        uiState = uiState,
        onBoundsChanged = { bounds ->
            viewModel.dispatch(MapIntent.OnBoundsChanged(bounds))
        },
        onShopSelected = { shop ->
            viewModel.dispatch(MapIntent.OnShopSelected(shop))
        },
        onShopDetailDismissed = {
            viewModel.dispatch(MapIntent.OnShopDetailDismissed)
        },
    )
}

@Composable
private fun MapScreen(
    uiState: MapUiState,
    onBoundsChanged: (MapBounds) -> Unit,
    onShopSelected: (RamenShop) -> Unit,
    onShopDetailDismissed: () -> Unit,
) {
    val selectedShop = uiState.selectedShop as? RamenShopSelectState.Selected

    Box(modifier = Modifier.fillMaxSize()) {
        KakaoMapView(
            modifier = Modifier.fillMaxSize(),
            shops = uiState.shops,
            onBoundsChanged = onBoundsChanged,
            onShopClick = onShopSelected,
        )

        CommonBottomSheet(
            visible = selectedShop != null,
            onDismissRequest = onShopDetailDismissed,
            config = CommonBottomSheetConfig(),
            content = {
                selectedShop?.let { state ->
                    RamenShopDetailContent(
                        shop = state.value,
                        waitingSystem = uiState.shopWaiting[state.value.id],
                    )
                }
            },
        )
    }
}
