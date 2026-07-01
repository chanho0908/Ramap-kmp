package com.peto.ramap.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.peto.ramap.designsystem.bottomsheet.CommonBottomSheet
import com.peto.ramap.designsystem.bottomsheet.CommonBottomSheetConfig
import com.peto.ramap.domain.model.MapBounds
import com.peto.ramap.domain.model.RamenShop
import com.peto.ramap.ui.map.component.RamenShopDetailContent
import com.peto.ramap.ui.map.component.RamenShopSearchBar
import com.peto.ramap.ui.map.component.RamenShopSearchResultList
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
        onShopSelected = { shop ->
            viewModel.dispatch(MapIntent.OnShopSelected(shop))
        },
        onShopDetailDismissed = {
            viewModel.dispatch(MapIntent.OnShopDetailDismissed)
        },
        onQueryChanged = { query ->
            viewModel.dispatch(MapIntent.OnQueryChanged(query))
        },
        onSearchResultsDismissed = {
            viewModel.dispatch(MapIntent.OnSearchResultsDismissed)
        },
    )
}

@Composable
private fun MapScreen(
    uiState: MapUiState,
    onBoundsChanged: (MapBounds) -> Unit,
    onShopSelected: (RamenShop) -> Unit,
    onShopDetailDismissed: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onSearchResultsDismissed: () -> Unit,
) {
    val selectedShop: RamenShop? = uiState.selectedShop
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val isImeVisible = WindowInsets.ime.getBottom(density) > 0
    var wasImeVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isImeVisible) {
        if (wasImeVisible && !isImeVisible) {
            focusManager.clearFocus()
        }
        wasImeVisible = isImeVisible
    }

    Box(modifier = Modifier.fillMaxSize()) {
        KakaoMapView(
            modifier = Modifier.fillMaxSize(),
            shops = uiState.markerShops,
            focusShops = uiState.focusShops,
            onBoundsChanged = onBoundsChanged,
            onShopClick = onShopSelected,
        )

        RamenShopSearchBar(
            query = uiState.query,
            onQueryChange = onQueryChanged,
            modifier =
                Modifier
                    .padding(
                        top =
                            WindowInsets.statusBars
                                .asPaddingValues()
                                .calculateTopPadding() + 16.dp,
                    ).padding(horizontal = 10.dp),
        )

        CommonBottomSheet(
            visible = uiState.showBottomSheet,
            onDismissRequest = {
                if (selectedShop != null) {
                    onShopDetailDismissed()
                } else {
                    onSearchResultsDismissed()
                }
            },
            config = CommonBottomSheetConfig(),
            content = {
                when {
                    selectedShop != null -> {
                        RamenShopDetailContent(
                            shop = selectedShop,
                            waitingSystem = uiState.shopWaiting[selectedShop.id],
                        )
                    }

                    uiState.showSearchResults -> {
                        RamenShopSearchResultList(
                            shops = uiState.searchResultShops,
                            onShopClick = onShopSelected,
                        )
                    }
                }
            },
        )
    }
}
