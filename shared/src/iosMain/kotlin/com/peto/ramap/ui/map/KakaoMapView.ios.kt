package com.peto.ramap.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.peto.ramap.domain.model.MapBounds
import com.peto.ramap.domain.model.RamenShop
import com.peto.ramap.domain.model.RamenShops
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalComposeUiApi::class, ExperimentalForeignApi::class)
@Composable
actual fun KakaoMapView(
    shops: RamenShops,
    focusShops: List<RamenShop>,
    onBoundsChanged: (MapBounds) -> Unit,
    onShopClick: (RamenShop) -> Unit,
    modifier: Modifier,
) {
    val mapController =
        remember {
            IosKakaoMapController(
                onBoundsChanged = onBoundsChanged,
                onShopClick = onShopClick,
            )
        }

    UIKitView(
        modifier = modifier,
        factory = {
            mapController.view
        },
        update = {
            mapController.updateShops(shops)
            mapController.updateFocusShops(focusShops)
        },
        properties =
            UIKitInteropProperties(
                interactionMode = UIKitInteropInteractionMode.NonCooperative,
                isNativeAccessibilityEnabled = false,
            ),
    )

    DisposableEffect(Unit) {
        onDispose {
            mapController.dispose()
        }
    }
}
