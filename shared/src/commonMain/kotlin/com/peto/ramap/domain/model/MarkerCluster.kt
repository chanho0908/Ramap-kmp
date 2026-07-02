package com.peto.ramap.domain.model

import kotlin.math.floor

/**
 * 현재 지도 영역과 화면 크기를 기준으로 매장 마커와 클러스터 마커를 계산한다.
 */
class MarkerCluster {
    fun clustering(
        shops: RamenShops,
        bounds: MapBounds?,
        viewportWidth: Int,
        viewportHeight: Int,
    ): List<Marker> {
        if (shops.isEmpty()) return emptyList()
        if (bounds == null || checkViewportValidation(viewportWidth, viewportHeight)) {
            return createSingleMarkers(shops)
        }

        val cellSize = createCellSize(bounds, viewportWidth, viewportHeight)
        val groups = groupShopsByCell(shops, bounds, cellSize)
        return groups.map(::createMarker)
    }

    /**
     * 클러스터 계산에 사용할 수 없는 화면 크기인지 확인한다.
     */
    private fun checkViewportValidation(
        viewportWidth: Int,
        viewportHeight: Int,
    ): Boolean = viewportWidth <= 0 || viewportHeight <= 0

    /**
     * 모든 매장을 단일 매장 마커로 변환한다.
     */
    private fun createSingleMarkers(shops: RamenShops): List<Marker> = shops.map { shop -> Marker.SingleMarker(shop.value) }

    /**
     * 화면상 같은 크기로 보일 grid cell의 위경도 크기를 계산한다.
     */
    private fun createCellSize(
        bounds: MapBounds,
        viewportWidth: Int,
        viewportHeight: Int,
    ): ClusterCellSize =
        ClusterCellSize(
            latitude = bounds.latSpan * CLUSTER_CELL_SIZE_PX / viewportHeight,
            longitude = bounds.lngSpan * CLUSTER_CELL_SIZE_PX / viewportWidth,
        )

    /**
     * 각 매장이 들어갈 grid cell key를 기준으로 매장을 묶는다.
     */
    private fun groupShopsByCell(
        shops: RamenShops,
        bounds: MapBounds,
        cellSize: ClusterCellSize,
    ): Collection<List<RamenShop>> = shops.values.groupBy { shop -> createCellKey(shop, bounds, cellSize) }.values

    /**
     * 매장의 위경도를 현재 bounds 기준 grid cell key로 변환한다.
     */
    private fun createCellKey(
        shop: RamenShop,
        bounds: MapBounds,
        cellSize: ClusterCellSize,
    ): Pair<Int, Int> {
        val latIndex = floor((shop.location.lat - bounds.minLat) / cellSize.latitude).toInt()
        val lngIndex = floor((shop.location.lng - bounds.minLng) / cellSize.longitude).toInt()
        return latIndex to lngIndex
    }

    /**
     * 같은 cell에 있는 매장 그룹을 단일 마커 또는 클러스터 마커로 변환한다.
     */
    private fun createMarker(shops: List<RamenShop>): Marker {
        if (shops.size == 1) return Marker.SingleMarker(shops.first())
        val sortedShops = shops.sortedBy { shop -> shop.id }

        return Marker.ClusterMaker(
            id = createClusterId(sortedShops),
            location = createClusterLocation(sortedShops),
            shops = sortedShops,
        )
    }

    /**
     * 포함 매장 ID를 정렬해 입력 순서와 무관한 클러스터 ID를 만든다.
     */
    private fun createClusterId(shops: List<RamenShop>): String = shops.joinToString(separator = CLUSTER_ID_SEPARATOR) { shop -> shop.id }

    /**
     * 클러스터에 포함된 매장 좌표의 평균 위치를 계산한다.
     */
    private fun createClusterLocation(shops: List<RamenShop>): Location =
        Location(
            lat = shops.sumOf { shop -> shop.location.lat } / shops.size,
            lng = shops.sumOf { shop -> shop.location.lng } / shops.size,
        )

    companion object {
        private const val CLUSTER_CELL_SIZE_PX = 180.0
        private const val CLUSTER_ID_SEPARATOR = "-"
    }
}
