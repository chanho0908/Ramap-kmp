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
import com.kakao.vectormap.label.LabelManager
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextBuilder
import com.kakao.vectormap.label.LabelTextStyle
import com.peto.ramap.core.config.RamenShopMarkerConfig
import com.peto.ramap.domain.model.MapBounds
import com.peto.ramap.domain.model.RamenShop
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
    onShopClick: (RamenShop) -> Unit,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val mapView = remember { MapView(context) }
    val isMapStarted = remember { AtomicBoolean(false) }
    val kakaoMapState = remember { mutableStateOf<KakaoMap?>(null) }

    val markerBitmap = rememberRamenShopMarkerBitmap()
    val locationPermissionLauncher =
        rememberKakaoMapLocationPermissionLauncher(
            kakaoMapState = kakaoMapState,
            context = context,
        )

    BindMapViewLifecycle(
        mapView = mapView,
        isMapStarted = isMapStarted,
        lifecycle = lifecycle,
    )

    renderRamenShopMarkers(
        kakaoMap = kakaoMapState.value,
        markerBitmap = markerBitmap,
        shops = shops,
        onShopClick = onShopClick,
    )

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.startIfNeeded(
                isMapStarted = isMapStarted,
                lifecycle = lifecycle,
                context = context,
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
        markerPainter.toBitmap(
            density = density,
            width = RamenShopMarkerConfig.WIDTH,
            height = RamenShopMarkerConfig.HEIGHT,
        )
    }
}

@Composable
private fun rememberKakaoMapLocationPermissionLauncher(
    kakaoMapState: MutableState<KakaoMap?>,
    context: Context,
): ActivityResultLauncher<Array<String>> =
    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        if (permissions.isLocationGranted()) {
            kakaoMapState.value?.moveToLastKnownLocation(context)
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
                        if (isMapStarted.get()) {
                            mapView.resume()
                        }
                    }

                    Lifecycle.Event.ON_PAUSE -> {
                        if (isMapStarted.get()) {
                            mapView.pause()
                        }
                    }

                    Lifecycle.Event.ON_DESTROY -> {
                        mapView.finish()
                    }

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

@Composable
private fun renderRamenShopMarkers(
    kakaoMap: KakaoMap?,
    markerBitmap: Bitmap,
    shops: RamenShops,
    onShopClick: (RamenShop) -> Unit,
) {
    val renderedShopIds = remember { mutableSetOf<String>() }

    LaunchedEffect(kakaoMap, markerBitmap, shops, onShopClick) {
        kakaoMap?.setOnLabelClickListener { _, _, label ->
            val shopId = label.tag as? String ?: return@setOnLabelClickListener false
            val shop = shops.value[shopId] ?: return@setOnLabelClickListener false
            onShopClick(shop)
            true
        }

        kakaoMap?.renderNewRamenShopMarkers(
            markerBitmap = markerBitmap,
            shops = shops,
            renderedShopIds = renderedShopIds,
        )
    }
}

private fun MapView.startIfNeeded(
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
            override fun onMapDestroy() = Unit

            override fun onMapError(error: Exception) = Unit
        },
        object : KakaoMapReadyCallback() {
            override fun onMapReady(kakaoMap: KakaoMap) {
                onMapReady(kakaoMap)

                kakaoMap.bindBoundsChangedListener(
                    mapView = this@startIfNeeded,
                    onBoundsChanged = onBoundsChanged,
                )

                post {
                    notifyCurrentBounds(
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

private fun KakaoMap.bindBoundsChangedListener(
    mapView: MapView,
    onBoundsChanged: (MapBounds) -> Unit,
) {
    setOnCameraMoveEndListener { map, _, _ ->
        mapView.notifyCurrentBounds(
            kakaoMap = map,
            onBoundsChanged = onBoundsChanged,
        )
    }
}

private fun MapView.notifyCurrentBounds(
    kakaoMap: KakaoMap,
    onBoundsChanged: (MapBounds) -> Unit,
) {
    kakaoMap
        .currentMapBounds(
            width = width,
            height = height,
        )?.let(onBoundsChanged)
}

private fun KakaoMap.currentMapBounds(
    width: Int,
    height: Int,
): MapBounds? {
    if (width <= 0 || height <= 0) return null

    val screenPoints =
        listOfNotNull(
            fromScreenPoint(0, 0),
            fromScreenPoint(width, 0),
            fromScreenPoint(0, height),
            fromScreenPoint(width, height),
        )

    if (screenPoints.size < 4) return null

    return MapBounds(
        minLat = screenPoints.minOf { it.latitude },
        maxLat = screenPoints.maxOf { it.latitude },
        minLng = screenPoints.minOf { it.longitude },
        maxLng = screenPoints.maxOf { it.longitude },
    )
}

private fun KakaoMap.moveToLastKnownLocationOrRequestPermission(
    context: Context,
    locationPermissionLauncher: ActivityResultLauncher<Array<String>>,
) {
    if (context.hasLocationPermission()) {
        moveToLastKnownLocation(context)
    } else {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
        )
    }
}

private fun KakaoMap.moveToLastKnownLocation(context: Context) {
    val location = lastKnownLocation(context) ?: return

    moveCamera(
        CameraUpdateFactory.newCenterPosition(
            LatLng.from(
                location.latitude,
                location.longitude,
            ),
        ),
    )
}

private fun KakaoMap.renderNewRamenShopMarkers(
    markerBitmap: Bitmap,
    shops: RamenShops,
    renderedShopIds: MutableSet<String>,
) {
    if (shops.value.isEmpty()) return

    val manager = labelManager ?: return
    val labelLayer = manager.layer ?: return
    val markerStyles = createRamenShopMarkerStyles(manager, markerBitmap)

    val newShops = shops.filterNotContainShops(renderedShopIds)

    if (newShops.isEmpty()) return

    val labelOptions =
        newShops.mapNotNull { shop ->
            markerStyles?.let { styles -> labelOptions(shop, styles) }
        }

    labelLayer.addLabels(labelOptions)
    renderedShopIds.addAll(newShops.map { it.id })
}

private fun createRamenShopMarkerStyles(
    manager: LabelManager,
    markerBitmap: Bitmap,
): LabelStyles? =
    manager.getLabelStyles(RamenShopMarkerConfig.STYLE_ID)
        ?: manager.addLabelStyles(
            LabelStyles.from(
                RamenShopMarkerConfig.STYLE_ID,
                LabelStyle
                    .from(markerBitmap)
                    .setAnchorPoint(0.5f, 1.0f)
                    .setTextStyles(
                        LabelTextStyle.from(
                            RamenShopMarkerConfig.LABEL_TEXT_SIZE,
                            RamenShopMarkerConfig.LABEL_TEXT_COLOR,
                            RamenShopMarkerConfig.LABEL_STROKE_WIDTH,
                            Color.WHITE,
                        ),
                    ),
            ),
        )

private fun labelOptions(
    shop: RamenShop,
    markerStyles: LabelStyles,
): LabelOptions =
    LabelOptions
        .from(
            "ramen-shop-${shop.id}",
            LatLng.from(
                shop.location.lat,
                shop.location.lng,
            ),
        ).setStyles(markerStyles)
        .setClickable(true)
        .setTag(shop.id)
        .setTexts(
            LabelTextBuilder().setTexts(shop.name),
        )

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

    CanvasDrawScope().draw(
        density = density,
        layoutDirection = LayoutDirection.Ltr,
        canvas = canvas,
        size =
            Size(
                imageBitmap.width.toFloat(),
                imageBitmap.height.toFloat(),
            ),
    ) {
        draw(size = size)
    }

    return bitmap
}

private fun Map<String, Boolean>.isLocationGranted(): Boolean =
    this[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
        this[Manifest.permission.ACCESS_COARSE_LOCATION] == true

@SuppressLint("MissingPermission")
private fun lastKnownLocation(context: Context): Location? {
    if (!context.hasLocationPermission()) return null

    val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

    return LOCATION_PROVIDERS
        .mapNotNull { provider ->
            if (locationManager.isProviderEnabled(provider)) {
                locationManager.getLastKnownLocation(provider)
            } else {
                null
            }
        }.maxByOrNull { it.time }
}

private val LOCATION_PROVIDERS =
    listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
    )
