package com.peto.ramap.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextBuilder
import com.kakao.vectormap.label.LabelTextStyle
import com.peto.ramap.domain.model.MapBounds
import com.peto.ramap.domain.model.RamenShops
import com.peto.ramap.extension.hasLocationPermission
import org.jetbrains.compose.resources.painterResource
import ramap.shared.generated.resources.Res
import ramap.shared.generated.resources.marker_ramen
import java.util.concurrent.atomic.AtomicBoolean

@Composable
actual fun KakaoMapView(
    shops: RamenShops,
    onBoundsChanged: (MapBounds) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mapView = remember { MapView(context) }
    val isMapStarted = remember { AtomicBoolean(false) }
    val kakaoMapState = remember { mutableStateOf<KakaoMap?>(null) }
    val markerBitmap = rememberRamenShopMarkerBitmap()
    val locationPermissionLauncher = rememberLocationPermissionLauncher(kakaoMapState, context)

    SyncRamenShopMarkers(
        kakaoMap = kakaoMapState.value,
        markerBitmap = markerBitmap,
        shops = shops,
    )

    BindMapViewLifecycle(
        mapView = mapView,
        isMapStarted = isMapStarted,
        lifecycle = lifecycleOwner.lifecycle,
    )

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.startMapIfNeeded(
                isMapStarted = isMapStarted,
                lifecycle = lifecycleOwner.lifecycle,
                context = context,
                locationPermissionLauncher = locationPermissionLauncher,
                onMapReady = { kakaoMapState.value = it },
                onBoundsChanged = onBoundsChanged,
            )
            mapView
        },
    )
}

private val LOCATION_PERMISSIONS =
    arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

private const val RAMEN_SHOP_MARKER_STYLE_ID = "ramen-shop-marker-style"
private const val RAMEN_SHOP_MARKER_WIDTH = 40
private const val RAMEN_SHOP_MARKER_HEIGHT = 46
private const val RAMEN_SHOP_LABEL_TEXT_SIZE = 22
private const val RAMEN_SHOP_LABEL_TEXT_COLOR = 0xFF333333.toInt()

@Composable
private fun rememberRamenShopMarkerBitmap(): Bitmap {
    val density = LocalDensity.current
    val markerPainter = painterResource(Res.drawable.marker_ramen)

    return remember(markerPainter, density) {
        markerPainter.toBitmap(
            density = density,
            width = RAMEN_SHOP_MARKER_WIDTH,
            height = RAMEN_SHOP_MARKER_HEIGHT,
        )
    }
}

@Composable
private fun rememberLocationPermissionLauncher(
    kakaoMapState: MutableState<KakaoMap?>,
    context: Context,
): ActivityResultLauncher<Array<String>> =
    rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        if (permissions.isLocationGranted()) {
            kakaoMapState.value?.moveToLastKnownLocation(context)
        }
    }

@Composable
private fun SyncRamenShopMarkers(
    kakaoMap: KakaoMap?,
    markerBitmap: Bitmap,
    shops: RamenShops,
) {
    LaunchedEffect(kakaoMap, markerBitmap, shops) {
        kakaoMap?.renderRamenShopMarkers(
            markerBitmap = markerBitmap,
            shops = shops,
        )
    }
}

@Composable
private fun BindMapViewLifecycle(
    mapView: MapView,
    isMapStarted: AtomicBoolean,
    lifecycle: Lifecycle,
) {
    DisposableEffect(lifecycle, mapView) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        if (isMapStarted.get()) mapView.resume()
                    }

                    Lifecycle.Event.ON_PAUSE -> {
                        if (isMapStarted.get()) mapView.pause()
                    }

                    Lifecycle.Event.ON_DESTROY -> mapView.finish()
                    else -> Unit
                }
            }

        lifecycle.addObserver(observer)

        onDispose {
            lifecycle.removeObserver(observer)
            isMapStarted.set(false)
            mapView.finish()
        }
    }
}

private fun MapView.startMapIfNeeded(
    isMapStarted: AtomicBoolean,
    lifecycle: Lifecycle,
    context: Context,
    locationPermissionLauncher: ActivityResultLauncher<Array<String>>,
    onMapReady: (KakaoMap) -> Unit,
    onBoundsChanged: (MapBounds) -> Unit,
) {
    if (!isMapStarted.compareAndSet(false, true)) return

    start(
        object : MapLifeCycleCallback() {
            override fun onMapDestroy() {
            }

            override fun onMapError(error: Exception) {
            }
        },
        object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                onMapReady(kakaoMap)
                kakaoMap.bindBoundsChanges(
                    mapView = this@startMapIfNeeded,
                    onBoundsChanged = onBoundsChanged,
                )
                post {
                    notifyBoundsChanged(
                        kakaoMap = kakaoMap,
                        onBoundsChanged = onBoundsChanged,
                    )
                }
                kakaoMap.moveToLastKnownLocationOrRequestPermission(
                    context = context,
                    locationPermissionLauncher = locationPermissionLauncher,
                )
            }
        },
    )

    if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        resume()
    }
}

private fun KakaoMap.bindBoundsChanges(
    mapView: MapView,
    onBoundsChanged: (MapBounds) -> Unit,
) {
    setOnCameraMoveEndListener { map, _, _ ->
        mapView.notifyBoundsChanged(
            kakaoMap = map,
            onBoundsChanged = onBoundsChanged,
        )
    }
}

private fun KakaoMap.moveToLastKnownLocationOrRequestPermission(
    context: Context,
    locationPermissionLauncher: ActivityResultLauncher<Array<String>>,
) {
    if (context.hasLocationPermission()) {
        moveToLastKnownLocation(context)
    } else {
        locationPermissionLauncher.launch(LOCATION_PERMISSIONS)
    }
}

private fun MapView.notifyBoundsChanged(
    kakaoMap: KakaoMap,
    onBoundsChanged: (MapBounds) -> Unit,
) {
    val bounds =
        kakaoMap.currentMapBounds(
            width = width,
            height = height,
        )
    if (bounds != null) {
        onBoundsChanged(bounds)
    }
}

private fun KakaoMap.currentMapBounds(
    width: Int,
    height: Int,
): MapBounds? {
    if (width <= 0 || height <= 0) return null

    val points =
        listOfNotNull(
            fromScreenPoint(0, 0),
            fromScreenPoint(width, 0),
            fromScreenPoint(0, height),
            fromScreenPoint(width, height),
        )
    if (points.size < 4) return null

    return MapBounds(
        minLat = points.minOf { it.latitude },
        maxLat = points.maxOf { it.latitude },
        minLng = points.minOf { it.longitude },
        maxLng = points.maxOf { it.longitude },
    )
}

private fun KakaoMap.moveToLastKnownLocation(context: Context) {
    val location = context.lastKnownLocation() ?: return
    moveCamera(
        CameraUpdateFactory.newCenterPosition(
            LatLng.from(
                location.latitude,
                location.longitude,
            ),
        ),
    )
}

private fun KakaoMap.renderRamenShopMarkers(
    markerBitmap: Bitmap,
    shops: RamenShops,
) {
    val manager = labelManager ?: return
    val labelLayer = manager.layer ?: return

    labelLayer.removeAll()

    if (shops.value.isEmpty()) return

    val markerStyles =
        manager.getLabelStyles(RAMEN_SHOP_MARKER_STYLE_ID)
            ?: manager.addLabelStyles(
                LabelStyles.from(
                    RAMEN_SHOP_MARKER_STYLE_ID,
                    LabelStyle
                        .from(markerBitmap)
                        .setAnchorPoint(0.5f, 1.0f)
                        .setTextStyles(
                            LabelTextStyle.from(
                                RAMEN_SHOP_LABEL_TEXT_SIZE,
                                RAMEN_SHOP_LABEL_TEXT_COLOR,
                                4,
                                Color.WHITE,
                            ),
                        ),
                ),
            )

    val labelOptions =
        shops.value.map { shop ->
            val position =
                LatLng.from(
                    shop.location.lat,
                    shop.location.lng,
                )
            val labelOptions =
                LabelOptions.from(
                    "ramen-shop-${shop.id}",
                    position,
                )

            labelOptions
                .setStyles(markerStyles)
                .setTexts(
                    LabelTextBuilder().setTexts(shop.name),
                )
        }

    labelLayer.addLabels(labelOptions)
}

private fun Painter.toBitmap(
    density: Density,
    width: Int,
    height: Int,
): Bitmap {
    val bitmap =
        Bitmap.createBitmap(
            (width * density.density).toInt(),
            (height * density.density).toInt(),
            Bitmap.Config.ARGB_8888,
        )
    val imageBitmap = bitmap.asImageBitmap()
    val canvas = Canvas(imageBitmap)

    val size =
        Size(
            imageBitmap.width.toFloat(),
            imageBitmap.height.toFloat(),
        )

    CanvasDrawScope().draw(
        density = density,
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size = size,
    ) {
        draw(
            size = this.size,
        )
    }

    return bitmap
}

private fun Map<String, Boolean>.isLocationGranted(): Boolean =
    this[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
        this[Manifest.permission.ACCESS_COARSE_LOCATION] == true

@SuppressLint("MissingPermission")
private fun Context.lastKnownLocation(): Location? {
    if (!hasLocationPermission()) return null

    val locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
    val providers =
        listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
        )

    return providers
        .mapNotNull { provider ->
            if (locationManager.isProviderEnabled(provider)) {
                locationManager.getLastKnownLocation(provider)
            } else {
                null
            }
        }.maxByOrNull { it.time }
}
