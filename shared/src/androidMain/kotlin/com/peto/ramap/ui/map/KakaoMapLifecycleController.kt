package com.peto.ramap.ui.map

import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.Lifecycle
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.KakaoMapReadyCallback
import com.kakao.vectormap.MapLifeCycleCallback
import com.kakao.vectormap.MapView
import com.peto.ramap.domain.model.MapBounds
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Android `MapView`와 KakaoMap SDK의 생명주기를 연결한다.
 *
 * 지도 시작, Compose 생명주기에 따른 resume/pause/finish, 지도 준비 이후 bounds 알림과
 * 초기 위치 이동 흐름을 한곳에서 관리한다.
 */
internal class KakaoMapLifecycleController(
    private val mapView: MapView,
    private val locationProvider: LocationProvider,
    private val boundsCalculator: MapBoundsCalculator,
    private val cameraController: KakaoCameraController,
) {
    private val isMapStarted = AtomicBoolean(false)

    fun startMap(
        lifecycle: Lifecycle,
        locationPermissionLauncher: ActivityResultLauncher<Array<String>>,
        onMapReady: (KakaoMap) -> Unit,
        onBoundsChanged: (MapBounds) -> Unit,
    ) {
        if (!isMapStarted.compareAndSet(false, true)) return

        startMap(onBoundsChanged, onMapReady, locationPermissionLauncher)

        if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            mapView.resume()
        }
    }

    private fun startMap(
        onBoundsChanged: (MapBounds) -> Unit,
        onMapReady: (KakaoMap) -> Unit,
        locationPermissionLauncher: ActivityResultLauncher<Array<String>>,
    ) {
        mapView.start(
            object : MapLifeCycleCallback() {
                override fun onMapDestroy() = Unit

                override fun onMapError(error: Exception) = Unit
            },
            object : KakaoMapReadyCallback() {
                override fun onMapReady(kakaoMap: KakaoMap) {
                    onMapReady(kakaoMap)
                    bindBoundsChangedListener(kakaoMap, onBoundsChanged)

                    mapView.post {
                        notifyCurrentBounds(kakaoMap, onBoundsChanged)
                    }

                    locationProvider.ensureLocationPermission(locationPermissionLauncher) {
                        locationProvider.moveToLastKnownLocation(kakaoMap, cameraController)
                    }
                }
            },
        )
    }

    fun resume() {
        if (isMapStarted.get()) {
            mapView.resume()
        }
    }

    fun pause() {
        if (isMapStarted.get()) {
            mapView.pause()
        }
    }

    fun finish() {
        isMapStarted.set(false)
        mapView.finish()
    }

    private fun bindBoundsChangedListener(
        kakaoMap: KakaoMap,
        onBoundsChanged: (MapBounds) -> Unit,
    ) {
        kakaoMap.setOnCameraMoveEndListener { map, _, _ ->
            notifyCurrentBounds(map, onBoundsChanged)
        }
    }

    private fun notifyCurrentBounds(
        kakaoMap: KakaoMap,
        onBoundsChanged: (MapBounds) -> Unit,
    ) {
        boundsCalculator
            .currentBounds(
                kakaoMap = kakaoMap,
                width = mapView.width,
                height = mapView.height,
            )?.let(onBoundsChanged)
    }
}
