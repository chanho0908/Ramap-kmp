package com.peto.ramap.ui.map

import android.location.Location
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.camera.CameraUpdateFactory
import com.peto.ramap.domain.model.RamenShop

/**
 * 카카오 지도 카메라 이동을 담당한다.
 *
 * 선택된 매장이 하나일 때는 중심 이동을, 여러 매장을 포커스할 때는 모든 매장이 보이도록
 * 카메라 영역을 맞춘다.
 */
internal class KakaoCameraController {
    private var lastFocusKey = ""

    /**
     * Compose 재구성으로 같은 포커스 요청이 반복될 수 있으므로,
     * 매장 id와 좌표로 만든 안정적인 key를 저장해 불필요한 카메라 이동을 막는다.
     */
    fun focusRamenShops(
        kakaoMap: KakaoMap,
        shops: List<RamenShop>,
    ) {
        val focusKey = focusKey(shops)
        if (focusKey.isBlank() || lastFocusKey == focusKey) return

        lastFocusKey = focusKey

        when (shops.size) {
            1 -> moveToShop(kakaoMap, shops.first())
            else -> fitShops(kakaoMap, shops)
        }
    }

    fun moveToLocation(
        kakaoMap: KakaoMap,
        location: Location,
    ) {
        kakaoMap.moveCamera(
            CameraUpdateFactory.newCenterPosition(
                LatLng.from(
                    location.latitude,
                    location.longitude,
                ),
            ),
        )
    }

    private fun moveToShop(
        kakaoMap: KakaoMap,
        shop: RamenShop,
    ) {
        kakaoMap.moveCamera(
            CameraUpdateFactory.newCenterPosition(shopLatLng(shop)),
        )
    }

    private fun fitShops(
        kakaoMap: KakaoMap,
        shops: List<RamenShop>,
    ) {
        kakaoMap.moveCamera(
            CameraUpdateFactory.fitMapPoints(
                shops.map(::shopLatLng).toTypedArray(),
                FOCUS_SHOPS_PADDING_PX,
            ),
        )
    }

    private fun shopLatLng(shop: RamenShop): LatLng =
        LatLng.from(
            shop.location.lat,
            shop.location.lng,
        )

    private fun focusKey(shops: List<RamenShop>): String =
        shops.joinToString(separator = "|") { shop ->
            "${shop.id}:${shop.location.lat}:${shop.location.lng}"
        }

    private companion object {
        private const val FOCUS_SHOPS_PADDING_PX = 120
    }
}
