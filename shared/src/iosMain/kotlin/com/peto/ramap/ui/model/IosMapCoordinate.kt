package com.peto.ramap.ui.model

import com.peto.ramap.ui.extension.toRadians
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Kakao Maps SDK에서 얻은 iOS 지도 좌표를 도메인 로직에서 다루기 쉽게 표현한 값 객체다.
 *
 * @property latitude WGS84 기준 위도.
 * @property longitude WGS84 기준 경도.
 */
data class IosMapCoordinate(
    val latitude: Double,
    val longitude: Double,
) {
    /**
     * 현재 좌표와 대상 위경도 사이의 대권 거리를 미터 단위로 계산한다.
     *
     * 지도 탭 위치에서 가장 가까운 매장 마커를 찾기 위해 haversine 공식을 사용한다.
     * 위경도 좌표는 평면이 아니라 둥근 지구 표면 위의 점이므로, 단순한 피타고라스 거리 대신
     * 두 좌표가 지구 중심에서 이루는 각도인 중심각을 구한 뒤 `지구 반지름 * 중심각`으로
     * 지표면 거리를 계산한다.
     *
     * `haversine`은 위도/경도 차이를 라디안 기준으로 조합한 중간값
     * `centralAngle`은 그 중간값을 실제 중심각으로 변환한 값
     *
     * @param targetLatitude 거리 계산 대상의 위도.
     * @param targetLongitude 거리 계산 대상의 경도.
     * @return 두 좌표 사이의 근사 거리. 단위는 미터다.
     */
    fun distanceTo(
        targetLatitude: Double,
        targetLongitude: Double,
    ): Double {
        val latDistance = (targetLatitude - latitude).toRadians()
        val lngDistance = (targetLongitude - longitude).toRadians()
        val originLat = latitude.toRadians()
        val targetLat = targetLatitude.toRadians()
        val haversine = haversine(latDistance, lngDistance, originLat, targetLat)
        val centralAngle = centralAngle(haversine)

        return distanceInMeters(centralAngle)
    }

    /**
     * 두 좌표의 라디안 차이를 지구 중심각 계산에 사용할 haversine 중간값으로 변환한다.
     */
    private fun haversine(
        latDistance: Double,
        lngDistance: Double,
        originLat: Double,
        targetLat: Double,
    ): Double =
        sin(latDistance / 2).pow(2) +
            cos(originLat) * cos(targetLat) * sin(lngDistance / 2).pow(2)

    /**
     * haversine 중간값을 두 좌표가 지구 중심에서 이루는 각도인 중심각으로 변환한다.
     */
    private fun centralAngle(haversine: Double): Double = 2 * atan2(sqrt(haversine), sqrt(1 - haversine))

    /**
     * 중심각을 지구 표면 위 거리로 변환한다.
     */
    private fun distanceInMeters(centralAngle: Double): Double = EARTH_RADIUS_METERS * centralAngle

    companion object {
        private const val EARTH_RADIUS_METERS = 6_371_000.0
    }
}
