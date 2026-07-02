package com.peto.ramap.ui.map

import android.graphics.Bitmap
import android.graphics.Color
import com.kakao.vectormap.KakaoMap
import com.kakao.vectormap.LatLng
import com.kakao.vectormap.label.LabelManager
import com.kakao.vectormap.label.LabelOptions
import com.kakao.vectormap.label.LabelStyle
import com.kakao.vectormap.label.LabelStyles
import com.kakao.vectormap.label.LabelTextBuilder
import com.kakao.vectormap.label.LabelTextStyle
import com.peto.ramap.core.config.RamenShopMarkerConfig
import com.peto.ramap.domain.model.RamenShop
import com.peto.ramap.domain.model.RamenShops

/**
 * 라멘 매장 마커를 KakaoMap label layer에 렌더링한다.
 *
 * 이미 렌더링한 매장 id를 기억해 새 마커만 추가하고, 검색 결과나 지도 영역 변경으로
 * 더 이상 보이면 안 되는 마커는 제거한다.
 */
internal class KakaoMarkerRenderer {
    private val renderedShopIds = mutableSetOf<String>()

    fun render(
        kakaoMap: KakaoMap,
        markerBitmap: Bitmap,
        shops: RamenShops,
        onShopClick: (RamenShop) -> Unit,
    ) {
        setUpKaKaoMapListener(kakaoMap, shops, onShopClick)

        val manager = kakaoMap.labelManager ?: return
        val labelLayer = manager.layer ?: return

        removeStaleMarkers(
            visibleShopIds = shops.keys,
            removeLabel = { shopId ->
                labelLayer.getLabel(markerLabelId(shopId))?.remove()
            },
        )

        if (shops.isEmpty()) return

        val markerStyles = createMarkerStyles(manager, markerBitmap)
        val newShops = shops.filterNotContainShops(renderedShopIds)

        if (newShops.isEmpty()) return

        val labelOptions =
            newShops.mapNotNull { shop ->
                markerStyles?.let { styles -> labelOptions(shop, styles) }
            }

        labelLayer.addLabels(labelOptions)
        renderedShopIds.addAll(newShops.map { it.id })
    }

    private fun setUpKaKaoMapListener(
        kakaoMap: KakaoMap,
        shops: RamenShops,
        onShopClick: (RamenShop) -> Unit,
    ) {
        kakaoMap.setOnLabelClickListener { _, _, label ->
            val shopId = label.tag as? String ?: return@setOnLabelClickListener false
            val shop = shops[shopId] ?: return@setOnLabelClickListener false
            onShopClick(shop)
            true
        }
    }

    /**
     * 검색 결과나 현재 지도 영역이 바뀌면 기존 label layer에는 더 이상 보이면 안 되는
     * 매장 마커가 남을 수 있으므로, 현재 visible set과 rendered set의 차이만 제거한다.
     */
    private fun removeStaleMarkers(
        visibleShopIds: Set<String>,
        removeLabel: (String) -> Unit,
    ) {
        val staleShopIds = renderedShopIds - visibleShopIds

        staleShopIds.forEach(removeLabel)
        renderedShopIds.removeAll(staleShopIds)
    }

    private fun createMarkerStyles(
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
                markerLabelId(shop.id),
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

    private fun markerLabelId(shopId: String): String = "ramen-shop-$shopId"
}
