package com.peto.ramap.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.peto.ramap.domain.model.MapBounds
import com.peto.ramap.domain.model.RamenShops
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
