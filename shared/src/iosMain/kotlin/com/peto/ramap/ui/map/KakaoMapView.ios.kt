package com.peto.ramap.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import cocoapods.KakaoMapsSDK.CompetitionTypeNone
import cocoapods.KakaoMapsSDK.CompetitionUnitPoi
import cocoapods.KakaoMapsSDK.KMController
import cocoapods.KakaoMapsSDK.KMViewContainer
import cocoapods.KakaoMapsSDK.KakaoMap
import cocoapods.KakaoMapsSDK.KakaoMapEventDelegateProtocol
import cocoapods.KakaoMapsSDK.LabelLayerOptions
import cocoapods.KakaoMapsSDK.LabelManager
import cocoapods.KakaoMapsSDK.MapControllerDelegateProtocol
import cocoapods.KakaoMapsSDK.MapPoint
import cocoapods.KakaoMapsSDK.MapviewInfo
import cocoapods.KakaoMapsSDK.MoveBy
import cocoapods.KakaoMapsSDK.OrderingTypeRank
import cocoapods.KakaoMapsSDK.PerLevelPoiStyle
import cocoapods.KakaoMapsSDK.PoiIconStyle
import cocoapods.KakaoMapsSDK.PoiOptions
import cocoapods.KakaoMapsSDK.PoiStyle
import cocoapods.KakaoMapsSDK.PoiText
import cocoapods.KakaoMapsSDK.PoiTextLineStyle
import cocoapods.KakaoMapsSDK.PoiTextStyle
import cocoapods.KakaoMapsSDK.PoiTransition
import cocoapods.KakaoMapsSDK.TextStyle
import cocoapods.KakaoMapsSDK.TransitionTypeNone
import cocoapods.KakaoMapsSDK.create
import com.peto.ramap.core.config.RamenShopMarkerConfig
import com.peto.ramap.domain.model.MapBounds
import com.peto.ramap.domain.model.RamenShops
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.cValue
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGPointMake
import platform.CoreGraphics.CGRectZero
import platform.UIKit.UIColor
import platform.UIKit.UIImage
import platform.UIKit.UIView
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

private const val DEFAULT_APP_NAME = "openmap"
private const val DEFAULT_VIEW_INFO_NAME = "map"
private const val DEFAULT_LONGITUDE = 127.108621
private const val DEFAULT_LATITUDE = 37.402005
private const val DEFAULT_ZOOM_LEVEL = 15
private const val MARKER_IMAGE_NAME = "marker_ramen"
private const val MARKER_LAYER_Z_ORDER = 10L
private const val MARKER_TEXT_RED = 0x33 / 255.0
private const val MARKER_TEXT_GREEN = 0x33 / 255.0
private const val MARKER_TEXT_BLUE = 0x33 / 255.0

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun KakaoMapView(
    shops: RamenShops,
    onBoundsChanged: (MapBounds) -> Unit,
    modifier: Modifier,
) {
    val mapController =
        remember {
            IosKakaoMapController(onBoundsChanged = onBoundsChanged)
        }

    UIKitView(
        modifier = modifier,
        factory = {
            mapController.view
        },
        update = {
            mapController.updateShops(shops)
        },
    )

    DisposableEffect(Unit) {
        onDispose {
            mapController.dispose()
        }
    }
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
private class IosKakaoMapController(
    private val onBoundsChanged: (MapBounds) -> Unit,
) : NSObject(),
    MapControllerDelegateProtocol,
    KakaoMapEventDelegateProtocol {
    private val mapViewContainer = KMViewContainer(frame = CGRectZero.readValue())
    val view = IosKakaoMapContainer(mapViewContainer) { startIfNeeded() }

    private val controller = KMController(viewContainer = mapViewContainer)
    private val mapViewName = "ramap"
    private val markerLayerId = "ramen-shop-marker-layer"
    private val renderedShopIds = mutableSetOf<String>()
    private var pendingShops: RamenShops? = null
    private var isStarted = false
    private var isMapViewAdded = false

    fun startIfNeeded() {
        if (isStarted || !hasMeasuredSize()) return

        controller.delegate = this
        val prepared = controller.prepareEngine()
        if (!prepared) return

        isStarted = true
        controller.activateEngine()
    }

    fun updateShops(shops: RamenShops) {
        pendingShops = shops
        renderRamenShopMarkers(shops)
    }

    fun dispose() {
        controller.pauseEngine()
        controller.resetEngine()
        controller.delegate = null
        isStarted = false
        isMapViewAdded = false
    }

    override fun addViews() {
        val defaultPosition =
            MapPoint(
                longitude = DEFAULT_LONGITUDE,
                latitude = DEFAULT_LATITUDE,
            )
        val viewInfo =
            MapviewInfo.create(
                mapViewName,
                DEFAULT_APP_NAME,
                DEFAULT_VIEW_INFO_NAME,
                defaultPosition,
                DEFAULT_ZOOM_LEVEL.toLong(),
                true,
            )

        controller.addView(viewInfo)
    }

    override fun addViewSucceeded(
        viewName: String,
        viewInfoName: String,
    ) {
        val kakaoMap = controller.getView(mapViewName) as? KakaoMap ?: return

        isMapViewAdded = true
        kakaoMap.eventDelegate = this
        notifyCurrentBounds(kakaoMap)
        pendingShops?.let(::renderRamenShopMarkers)
    }

    override fun cameraDidStoppedWithKakaoMap(
        kakaoMap: KakaoMap,
        by: MoveBy,
    ) {
        notifyCurrentBounds(kakaoMap)
    }

    private fun renderRamenShopMarkers(shops: RamenShops) {
        if (!isMapViewAdded) return

        val kakaoMap = controller.getView(mapViewName) as? KakaoMap ?: return
        val newShops = shops.filterNotContainShops(renderedShopIds)

        if (newShops.isEmpty()) return

        val labelManager = kakaoMap.getLabelManager()
        ensureMarkerStyle(labelManager)

        val layer =
            labelManager.getLabelLayerWithLayerID(markerLayerId)
                ?: labelManager.addLabelLayerWithOption(createMarkerLayerOptions()) ?: return
        layer.visible = true

        newShops.forEach { shop ->
            val option =
                PoiOptions(
                    styleID = RamenShopMarkerConfig.STYLE_ID,
                    poiID = "ramen-shop-${shop.id}",
                )

            option.addText(
                PoiText(
                    text = shop.name,
                    styleIndex = 0u,
                ),
            )

            val poi =
                layer.addPoiWithOption(
                    option = option,
                    at =
                        MapPoint(
                            longitude = shop.location.lng,
                            latitude = shop.location.lat,
                        ),
                    callback = null,
                )

            poi?.show()

            renderedShopIds += shop.id
        }
    }

    private fun ensureMarkerStyle(labelManager: LabelManager) {
        val image =
            UIImage.imageNamed(MARKER_IMAGE_NAME) ?: return
        val iconStyle =
            PoiIconStyle(
                image,
                CGPointMake(0.5, 1.0),
                poiTransition(),
                true,
                true,
                null,
            )
        val textLineStyle =
            PoiTextLineStyle()
                .apply {
                    textStyle =
                        TextStyle(
                            RamenShopMarkerConfig.LABEL_TEXT_SIZE.toULong(),
                            markerTextColor(),
                            RamenShopMarkerConfig.LABEL_STROKE_WIDTH.toULong(),
                            UIColor.whiteColor,
                            "",
                            0,
                            1.0f,
                            1.0f,
                        )
                }
        val textStyle =
            PoiTextStyle(
                poiTransition(),
                true,
                true,
                listOf(textLineStyle),
            )
        val poiStyle =
            PoiStyle(
                RamenShopMarkerConfig.STYLE_ID,
                listOf(
                    PerLevelPoiStyle(
                        iconStyle,
                        textStyle,
                        0.0f,
                        0,
                    ),
                ),
            )

        labelManager.addPoiStyle(poiStyle)
    }

    private fun poiTransition(): CValue<PoiTransition> =
        cValue {
            entrance = TransitionTypeNone
            exit = TransitionTypeNone
        }

    private fun createMarkerLayerOptions(): LabelLayerOptions =
        LabelLayerOptions(
            markerLayerId,
            CompetitionTypeNone,
            CompetitionUnitPoi,
            OrderingTypeRank,
            MARKER_LAYER_Z_ORDER,
        )

    private fun notifyCurrentBounds(kakaoMap: KakaoMap) {
        val width = mapViewContainer.bounds.useContents { size.width }
        val height = mapViewContainer.bounds.useContents { size.height }
        if (width <= 0.0 || height <= 0.0) return

        val points =
            listOf(
                kakaoMap.getPosition(CGPointMake(0.0, 0.0)),
                kakaoMap.getPosition(CGPointMake(width, 0.0)),
                kakaoMap.getPosition(CGPointMake(0.0, height)),
                kakaoMap.getPosition(CGPointMake(width, height)),
            ).map { point ->
                point.wgsCoord.useContents {
                    IosMapCoordinate(
                        latitude = latitude,
                        longitude = longitude,
                    )
                }
            }

        onBoundsChanged(
            MapBounds(
                minLat = points.minOf { it.latitude },
                maxLat = points.maxOf { it.latitude },
                minLng = points.minOf { it.longitude },
                maxLng = points.maxOf { it.longitude },
            ),
        )
    }

    private data class IosMapCoordinate(
        val latitude: Double,
        val longitude: Double,
    )

    private fun hasMeasuredSize(): Boolean =
        mapViewContainer.bounds.useContents {
            size.width > 0.0 && size.height > 0.0
        }

    private fun markerTextColor(): UIColor =
        UIColor.colorWithRed(
            red = MARKER_TEXT_RED,
            green = MARKER_TEXT_GREEN,
            blue = MARKER_TEXT_BLUE,
            alpha = 1.0,
        )
}

@OptIn(ExperimentalForeignApi::class)
private class IosKakaoMapContainer(
    private val mapViewContainer: KMViewContainer,
    private val onMeasured: () -> Unit,
) : UIView(frame = CGRectZero.readValue()) {
    init {
        addSubview(mapViewContainer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        mapViewContainer.setFrame(bounds)
        mapViewContainer.layoutIfNeeded()
        dispatch_async(dispatch_get_main_queue()) {
            onMeasured()
        }
    }
}
