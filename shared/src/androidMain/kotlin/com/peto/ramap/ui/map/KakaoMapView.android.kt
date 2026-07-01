package com.peto.ramap.ui.map

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.MapView
import com.peto.ramap.core.config.RamenShopMarkerConfig
import com.peto.ramap.domain.model.MapBounds
import com.peto.ramap.domain.model.RamenShop
import com.peto.ramap.domain.model.RamenShops
import org.jetbrains.compose.resources.painterResource
import ramap.shared.generated.resources.Res
import ramap.shared.generated.resources.marker_ramen

@Composable
actual fun KakaoMapView(
    shops: RamenShops,
    onBoundsChanged: (MapBounds) -> Unit,
    onShopClick: (RamenShop) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val mapView = remember { MapView(context) }
    val kakaoMapState = remember { mutableStateOf<KakaoMap?>(null) }
    val markerBitmap = rememberRamenShopMarkerBitmap()
    val currentOnShopClick = rememberUpdatedState(onShopClick)

    val locationProvider = remember(context) { LocationProvider(context) }
    val boundsCalculator = remember { MapBoundsCalculator() }
    val cameraController = remember { KakaoCameraController() }
    val markerRenderer = remember { KakaoMarkerRenderer() }
    val lifecycleController =
        remember(mapView, locationProvider, boundsCalculator, cameraController) {
            KakaoMapLifecycleController(
                mapView = mapView,
                locationProvider = locationProvider,
                boundsCalculator = boundsCalculator,
                cameraController = cameraController,
            )
        }

    val locationPermissionLauncher =
        rememberKakaoMapLocationPermissionLauncher(
            kakaoMapState = kakaoMapState,
            locationProvider = locationProvider,
            cameraController = cameraController,
        )

    BindMapViewLifecycle(
        controller = lifecycleController,
        lifecycle = lifecycle,
    )

    LaunchedEffect(kakaoMapState.value, markerBitmap, shops) {
        val kakaoMap = kakaoMapState.value ?: return@LaunchedEffect
        markerRenderer.render(
            kakaoMap = kakaoMap,
            markerBitmap = markerBitmap,
            shops = shops,
            onShopClick = { shop -> currentOnShopClick.value(shop) },
        )
    }

    AndroidView(
        modifier = modifier,
        factory = {
            lifecycleController.startMap(
                lifecycle = lifecycle,
                locationPermissionLauncher = locationPermissionLauncher,
                onMapReady = { kakaoMap ->
                    kakaoMapState.value = kakaoMap
                },
                onBoundsChanged = onBoundsChanged,
            )
            mapView
        },
    )
}

@Composable
private fun rememberRamenShopMarkerBitmap(): Bitmap {
    val density = LocalDensity.current
    val markerPainter = painterResource(Res.drawable.marker_ramen)

    return remember(markerPainter, density) {
        RamenShopMarkerBitmapFactory.create(
            painter = markerPainter,
            density = density,
            width = RamenShopMarkerConfig.WIDTH,
            height = RamenShopMarkerConfig.HEIGHT,
        )
    }
}

@Composable
private fun rememberKakaoMapLocationPermissionLauncher(
    kakaoMapState: MutableState<KakaoMap?>,
    locationProvider: LocationProvider,
    cameraController: KakaoCameraController,
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions(),
) { permissions ->
    if (locationProvider.isLocationGranted(permissions)) {
        val kakaoMap = kakaoMapState.value ?: return@rememberLauncherForActivityResult
        locationProvider.moveToLastKnownLocation(
            kakaoMap = kakaoMap,
            cameraController = cameraController,
        )
    }
}

@Composable
private fun BindMapViewLifecycle(
    controller: KakaoMapLifecycleController,
    lifecycle: Lifecycle,
) {
    DisposableEffect(lifecycle, controller) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> controller.resume()
                    Lifecycle.Event.ON_PAUSE -> controller.pause()
                    Lifecycle.Event.ON_DESTROY -> controller.finish()
                    else -> Unit
                }
            }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            controller.finish()
        }
    }
}
