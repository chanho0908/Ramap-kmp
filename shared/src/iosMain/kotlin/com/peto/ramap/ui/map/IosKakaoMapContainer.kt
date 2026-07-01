package com.peto.ramap.ui.map

import cocoapods.KakaoMapsSDK.KMViewContainer
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.CoreGraphics.CGPoint
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIEvent
import platform.UIKit.UITouch
import platform.UIKit.UIView
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * Compose의 [UIKitView] 안에서 Kakao Maps SDK의 [KMViewContainer]를 담는 UIKit 컨테이너
 *
 * Compose가 전달한 레이아웃 크기를 Kakao 지도 컨테이너에 반영하고, 지도 엔진이 실제 크기를
 * 가진 뒤 시작될 수 있도록 [onMeasured]를 호출한다.
 *
 * Kakao Maps SDK의 POI 탭 콜백이 iOS interop 환경에서 안정적으로 전달되지 않는 경우를 보완하기 위해
 * 단일 탭 위치도 [onTap]으로 전달한다.
 *
 * @param mapViewContainer Kakao Maps SDK가 지도 렌더링에 사용하는 네이티브 컨테이너.
 * @param onMeasured 컨테이너 크기 측정과 레이아웃 적용이 끝난 뒤 호출되는 콜백.
 * @param onTap 사용자가 지도 위를 한 번 탭했을 때 탭 좌표를 전달하는 콜백.
 */
@OptIn(ExperimentalForeignApi::class)
class IosKakaoMapContainer(
    private val mapViewContainer: KMViewContainer,
    private val onMeasured: () -> Unit,
    private val onTap: (CValue<CGPoint>) -> Unit,
) : UIView(frame = CGRectZero.readValue()) {
    init {
        addSubview(mapViewContainer)
    }

    /**
     * UIKit 레이아웃 단계에서 Kakao 지도 컨테이너의 frame을 현재 뷰 bounds에 맞춘다.
     *
     * Kakao 지도 엔진은 0 크기 뷰에서는 정상적으로 준비되지 않으므로, 다음 메인 큐 턴에서
     * [onMeasured]를 호출해 측정 완료 이후 엔진 시작을 시도한다.
     */
    override fun layoutSubviews() {
        super.layoutSubviews()
        mapViewContainer.setFrame(bounds)
        mapViewContainer.layoutIfNeeded()
        dispatch_async(dispatch_get_main_queue()) {
            onMeasured()
        }
    }

    /**
     * 지도 위 단일 탭을 Kakao 지도 좌표 변환에 사용할 수 있는 UIKit 좌표로 전달한다.
     */
    override fun touchesEnded(
        touches: Set<*>,
        withEvent: UIEvent?,
    ) {
        super.touchesEnded(touches, withEvent)

        val touch = touches.firstOrNull() as? UITouch ?: return
        if (touch.tapCount.toInt() != 1) return

        onTap(touch.locationInView(mapViewContainer))
    }
}
