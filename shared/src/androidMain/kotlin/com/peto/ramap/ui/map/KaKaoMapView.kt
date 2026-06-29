package com.peto.ramap.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.peto.ramap.domain.model.MapBounds
import com.peto.ramap.domain.model.RamenShops
import com.peto.ramap.extension.hasLocationPermission
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
    val locationPermissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions(),
        ) { permissions ->
            val isGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (isGranted) {
                kakaoMapState.value?.moveToLastKnownLocation(context)
            }
        }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer =
            LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        if (isMapStarted.get()) mapView.resume()
                    }

                    Lifecycle.Event.ON_PAUSE -> {
                        if (isMapStarted.get()) mapView.pause()
                    }

                    Lifecycle.Event.ON_DESTROY -> {
                        mapView.finish()
                    }

                    else -> Unit
                }
            }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            isMapStarted.set(false)
            mapView.finish()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.apply {
                if (isMapStarted.compareAndSet(false, true)) {
                    start(
                        object : MapLifeCycleCallback() {
                            override fun onMapDestroy() {
                            }

                            override fun onMapError(error: Exception) {
                            }
                        },
                        object : KakaoMapReadyCallback() {
                            override fun onMapReady(kakaoMap: KakaoMap) {
                                kakaoMapState.value = kakaoMap

                                mapView.post {
                                    val bounds =
                                        kakaoMap.currentMapBounds(
                                            width = mapView.width,
                                            height = mapView.height,
                                        )
                                    if (bounds != null) {
                                        onBoundsChanged(bounds)
                                    }
                                }

                                if (context.hasLocationPermission()) {
                                    kakaoMap.moveToLastKnownLocation(context)
                                } else {
                                    locationPermissionLauncher.launch(LOCATION_PERMISSIONS)
                                }

                                kakaoMap.setOnCameraMoveEndListener { map, _, _ ->
                                    val bounds =
                                        map.currentMapBounds(
                                            width = mapView.width,
                                            height = mapView.height,
                                        )
                                    if (bounds != null) {
                                        onBoundsChanged(bounds)
                                    }
                                }
                            }
                        },
                    )
                    if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                        resume()
                    }
                }
            }
        },
    )
}

private val LOCATION_PERMISSIONS =
    arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )

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
