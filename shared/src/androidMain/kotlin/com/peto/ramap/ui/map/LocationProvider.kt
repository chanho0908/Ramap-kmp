package com.peto.ramap.ui.map

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.kakao.vectormap.KakaoMap

/**
 * 지도에서 사용하는 위치 권한과 마지막 위치 조회를 캡슐화한다.
 *
 * 권한이 이미 있으면 마지막 위치로 이동하고, 권한이 없으면 Compose launcher를 통해
 * 권한 요청을 시작한다.
 */
internal class LocationProvider(
    private val context: Context,
) {
    /**
     * 지도 초기화 시 마지막 위치로 이동하거나 위치 권한을 요청한다.
     * 권한 결과가 도착하면 composable의 launcher callback에서 다시 마지막 위치 이동을 시도한다.
     */
    fun ensureLocationPermission(
        permissionLauncher: ActivityResultLauncher<Array<String>>,
        onGranted: () -> Unit,
    ) {
        if (hasLocationPermission()) {
            onGranted()
        } else {
            permissionLauncher.launch(LOCATION_PERMISSIONS)
        }
    }

    fun moveToLastKnownLocation(
        kakaoMap: KakaoMap,
        cameraController: KakaoCameraController,
    ) {
        val location = lastKnownLocation() ?: return
        cameraController.moveToLocation(kakaoMap, location)
    }

    fun isLocationGranted(permissions: Map<String, Boolean>): Boolean =
        permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

    private fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    private fun lastKnownLocation(): Location? {
        if (!hasLocationPermission()) return null

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

    private companion object {
        private val LOCATION_PERMISSIONS =
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )

        private val LOCATION_PROVIDERS =
            listOf(
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER,
            )
    }
}
